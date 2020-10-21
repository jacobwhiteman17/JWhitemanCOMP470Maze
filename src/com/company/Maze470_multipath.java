package com.company;
import java.awt.*;
import javax.swing.*;

public class Maze470_multipath
{
    public static final int MWIDTH=30,MHEIGHT=30,BLOCK=20;
    public static boolean robotActive=true;
    public static final int SPEED=100;

    public static final int LEFT=4,RIGHT=8,UP=1,DOWN=2;
    //1=wall above, 2=wall below, 4=wall on left, 8=wall on right, 16=not included in maze yet

    static int[][] maze;
    static MazeComponent mazecomp;

    //current position of robot
    static int robotX=0,robotY=0;

    //true means that a "crumb" is shown in the room
    static boolean[][] crumbs;

    public static void main(String[] args)
    {
        //make a maze array and a crumb array
        maze=new int[MWIDTH][MHEIGHT];
        crumbs=new boolean[MWIDTH][MHEIGHT];
        //set each room to be surrounded by walls and not part of the maze
        for (int i=0; i<MWIDTH; i++)
            for (int j=0; j<MHEIGHT; j++)
            {
                maze[i][j]=31;
                crumbs[i][j]=false;
            }

        //generate the maze
        makeMaze();

        //knock down up to 100 walls
        for(int i=0; i<100; i++)
        {
            int x=(int)(Math.random()*(MWIDTH-2));
            int y=(int)(Math.random()*MHEIGHT);
            if((maze[x][y]&RIGHT)!=0)
            {
                maze[x][y]^=RIGHT;
                maze[x+1][y]^=LEFT;
            }
        }

        JFrame f = new JFrame();
        f.setSize(MWIDTH*BLOCK+15,MHEIGHT*BLOCK+30);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setTitle("Maze!");
        mazecomp=new MazeComponent();
        f.add(mazecomp);
        f.setVisible(true);


        //have the robot wander around in its own thread
        if(robotActive)
        {
            new Thread(new Runnable(){
                public void run() {
                    int[] test = new int[]{RIGHT,DOWN,DOWN,LEFT,UP};
                    doMazeGuided(test);
                    //doMazeRandomWalk();
                }
            }).start();
        }
    }

    public static void makeMaze()
    {
        int[] blockListX = new int[MWIDTH*MHEIGHT];
        int[] blockListY = new int[MWIDTH*MHEIGHT];
        int blocks=0;
        int x,y;

        //Choose random starting block and add it to maze
        x=(int)(Math.random()*(MWIDTH-2)+1);
        y=(int)(Math.random()*(MHEIGHT-2)+1);
        maze[x][y]^=16;

        //Add all adjacent blocks to blocklist
        if (x>0)
        {
            blockListX[blocks]=x-1;
            blockListY[blocks]=y;
            blocks++;
        }
        if (x<MWIDTH-1)
        {
            blockListX[blocks]=x+1;
            blockListY[blocks]=y;
            blocks++;
        }
        if (y>0)
        {
            blockListX[blocks]=x;
            blockListY[blocks]=y-1;
            blocks++;
        }
        if (y<MHEIGHT-1)
        {
            blockListX[blocks]=x;
            blockListY[blocks]=y+1;
            blocks++;
        }

        //approach:
        // start with a single room in maze and all neighbors of the room in the "blocklist"
        // choose a room that is not yet part of the maze but is adjacent to the maze
        // add it to the maze by breaking a wall
        // put all of its neighbors that aren't in the maze into the "blocklist"
        // repeat until everybody is in the maze
        while (blocks>0)
        {
            //choose a random block from blocklist
            int b = (int)(Math.random()*blocks);

            //find which block in the maze it is adjacent to
            //and remove that wall
            x=blockListX[b];
            y=blockListY[b];

            //get a list of all of its neighbors that aren't in the maze
            int[] dir=new int[4];
            int numdir=0;

            //left
            if (x>0 && (maze[x-1][y]&16)==0)
            {
                dir[numdir++]=0;
            }
            //right
            if (x<MWIDTH-1 && (maze[x+1][y]&16)==0)
            {
                dir[numdir++]=1;
            }
            //up
            if (y>0 && (maze[x][y-1]&16)==0)
            {
                dir[numdir++]=2;
            }
            //down
            if (y<MHEIGHT-1 && (maze[x][y+1]&16)==0)
            {
                dir[numdir++]=3;
            }

            //choose one at random
            int d = (int)(Math.random()*numdir);
            d=dir[d];

            //tear down the wall
            //left
            if (d==0)
            {
                maze[x][y]^=LEFT;
                maze[x-1][y]^=RIGHT;
            }
            //right
            else if (d==1)
            {
                maze[x][y]^=RIGHT;
                maze[x+1][y]^=LEFT;
            }
            //up
            else if (d==2)
            {
                maze[x][y]^=UP;
                maze[x][y-1]^=DOWN;
            }
            //down
            else if (d==3)
            {
                maze[x][y]^=DOWN;
                maze[x][y+1]^=UP;
            }

            //set that block as "in the maze"
            maze[x][y]^=16;

            //remove it from the block list
            for (int j=0; j<blocks; j++)
            {
                if ((maze[blockListX[j]][blockListY[j]]&16)==0)
                {
                    for (int i=j; i<blocks-1; i++)
                    {
                        blockListX[i]=blockListX[i+1];
                        blockListY[i]=blockListY[i+1];
                    }
                    blocks--;
                    j=0;
                }
            }

            //put all adjacent blocks that aren't in the maze in the block list
            if (x>0 && (maze[x-1][y]&16)>0)
            {
                blockListX[blocks]=x-1;
                blockListY[blocks]=y;
                blocks++;
            }
            if (x<MWIDTH-1 && (maze[x+1][y]&16)>0)
            {
                blockListX[blocks]=x+1;
                blockListY[blocks]=y;
                blocks++;
            }
            if (y>0 && (maze[x][y-1]&16)>0)
            {
                blockListX[blocks]=x;
                blockListY[blocks]=y-1;
                blocks++;
            }
            if (y<MHEIGHT-1 && (maze[x][y+1]&16)>0)
            {
                blockListX[blocks]=x;
                blockListY[blocks]=y+1;
                blocks++;
            }
        }

        //remove top left and bottom right edges
//		maze[0][0]^=LEFT;    //commented out for now so that robot doesn't run out the entrance
        maze[MWIDTH-1][MHEIGHT-1]^=RIGHT;
    }

    //1. doGuided working
    //2.  make Maze state:
    //using the eight puzzle as a template: State(), isSolved(), canMove(int,int), allmoves(), move(int,int), alladjacent()
    //3.  dfs.  print out with println each room you visit, make sure it gets to 29,29
    //4.  put in the parent state link, after dfs while loop, push states and then pop them all
    //5.  call doGuided( with directions ) - does dfs work?
    //6.  put in the manhattan and do astar

    //- can move method: take a dir as input, returns boolean
    //from canmove, build an allmoves (from python example in class)

    //public State[] adjacentStates()
    //		{
    //			//go through each valid direction, get the state, make an array of these, return it
    //			int[] dirs=allMoves();
    //			//make an array of states, same size
    //			State[] adj=new State[dirs.length];
    //			//call move on each valid direction
    //			for(int i=0; i<dirs.length; i++)
    //				adj[i]=move(dirs[i]);
    //			return adj;
    //		}

    //public static void DFS(int x, int y){
    // State start = new State(x,y);
    //ArrayList<State> queue = new ArrayList<State>();
    //boolean[][] visited = new boolean[MWIDTH}[MHEIGHT];
    //queue.add(start);
    //while(queue.size()>0){
    //  current=queue.remove(queue.size()-1)
    //  visited[current.x][current.y]=true
    //  get adjacent states
    //  for each neighbor{
    //      if(visited[neightbor.x][neighbor.y]==false];
    //      queue.add(neighbor);
    //A, B, G, F, C, D, Exit
    //Exit --> D ->>C -->B --> A
    //push: e, d, c, ,b, a
    //pop: a, b, c, d,e
    //  }
    //first: in Maze, go to your state class
    //add on a
    //State parent
    //int direction
    //move method in state:
    //child.parent=this;
    //child=direction=direction;
    //back to dfs, right at end of while loop
    //make a Stack
    //work backwards: current=current.parent, and push to stack
    //pop from the stack, print it out
    //doMazeGuided(int[] dir)
    //once stack is full, before we pop, make an int[] dir
    //int[] dir=new int[flip.size()]
    //count=0;
    //dir[count++] = flip.pop().direction
    //doMazeGuided(dir)
    // }
    //
    // }

    //public int manhattan()
    //(MWIDTH-x) + (MHEGIHT-y)

    public static void doMazeGuided(int[] directionArray)
    {
        //directionArray = new int[]{LEFT,RIGHT,UP,DOWN};
        //int dir = RIGHT;

        while(robotX!=MWIDTH-1 || robotY!=MHEIGHT-1)
        {
            int x = robotX;
            int y = robotY;

            for(int i: directionArray)
            {
                if((maze[x][y]&i)==0)
                {
                    if(i==LEFT&&) robotX--;//if dir is left and there isn't a wall
                    if(i==RIGHT&&) robotX++;//if dir is right and there isn't a wall
                    if(i==UP&&) robotY--;//if dir is up and there isn't a wall
                    if(i==DOWN&&) robotY++;//if dir is down and there isn't a wall
                }
                /*if((maze[x][y]&i)!=0)
                {
                    if(i==LEFT) robotX++;
                    if(i==RIGHT) robotX--;
                    if(i==UP) robotY++;
                    if(i==DOWN) robotY--;
                }*/
            }
            crumbs[x][y]=true;

            mazecomp.repaint();
            try{ Thread.sleep(SPEED); } catch(Exception e) { }
        }
        //for each element in dir
        //if((maze[x][y]&dir)==0)
    }

    //the robot will wander around aimlessly until it happens to stumble on the exit
    public static void doMazeRandomWalk()
    {
        int dir=RIGHT;

        while(robotX!=MWIDTH-1 || robotY!=MHEIGHT-1)
        {
            int x=robotX;
            int y=robotY;

            //choose a direction at random
            dir=new int[]{LEFT,RIGHT,UP,DOWN}[(int)(Math.random()*4)];
            //move the robot
            if((maze[x][y]&dir)==0)
            {
                if(dir==LEFT) robotX--;
                if(dir==RIGHT) robotX++;
                if(dir==UP) robotY--;
                if(dir==DOWN) robotY++;
            }

            //leave a crumb
            crumbs[x][y]=true;

            //repaint and pause momentarily
            mazecomp.repaint();
            try{ Thread.sleep(SPEED); } catch(Exception e) { }
        }
        System.out.println("Done");
    }


    public static class MazeComponent extends JComponent
    {
        public void paintComponent(Graphics g)
        {
            g.setColor(Color.WHITE);
            g.fillRect(0,0,MWIDTH*BLOCK,MHEIGHT*BLOCK);
            g.setColor(new Color(100,0,0));
            for (int x=0; x<MWIDTH; x++)
            {
                for (int y=0; y<MHEIGHT; y++)
                {
                    if ((maze[x][y]&1)>0)
                        g.drawLine(x*BLOCK,y*BLOCK,x*BLOCK+BLOCK,y*BLOCK);
                    if ((maze[x][y]&2)>0)
                        g.drawLine(x*BLOCK,y*BLOCK+BLOCK,x*BLOCK+BLOCK,y*BLOCK+BLOCK);
                    if ((maze[x][y]&4)>0)
                        g.drawLine(x*BLOCK,y*BLOCK,x*BLOCK,y*BLOCK+BLOCK);
                    if ((maze[x][y]&8)>0)
                        g.drawLine(x*BLOCK+BLOCK,y*BLOCK,x*BLOCK+BLOCK,y*BLOCK+BLOCK);
                }
            }

            if (robotActive)
            {
                g.setColor(Color.BLUE);
                for (int x=0; x<MWIDTH; x++)
                {
                    for (int y=0; y<MHEIGHT; y++)
                    {
                        if (crumbs[x][y])
                            g.fillRect(x*BLOCK+BLOCK/2-1,y*BLOCK+BLOCK/2-1,2,2);
                    }
                }

                g.setColor(Color.GREEN);
                g.fillOval(robotX*BLOCK+1,robotY*BLOCK+1,BLOCK-2,BLOCK-2);
            }
        }
    }
}