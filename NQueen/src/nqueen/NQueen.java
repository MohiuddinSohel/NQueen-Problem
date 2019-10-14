/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nqueen;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author mahmed27
 */
public class NQueen {
    
    private int allowedSideWayMove = 200;
    public static int allowedNumberOfRandomRestart = 500;
    private int max_step = 1000;
    public static int MAINPROBLEM = 1;
    public static int BONUSPROBLEM = 2;
    private int totalRandomRestart;// tracks total random restart for a problem instance
    private int totalStepInARestart;// tracks total state visited for a problem instance

    /**
     * Starting point. take input from user queen number and problem instance to run
     * @param args(Array)
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner in = new Scanner(System.in);
        NQueen nqueen = new NQueen();
        while(true) {
            System.out.println("Insert the number of Queen(MUST be Integer without space, otherwise it will give NumberFormatException) in the board.\nTO QUIT INSERT 0(ZERO)");
            int numberOfQueen = in.nextInt();
            if(numberOfQueen  == 0) {
                break;
            }
            System.out.println("Which Instance of problem you want to Run?\nPress 1 for Main problem\nPress 2 for Bonus problem");
            int probInstance = in.nextInt();
            if(MAINPROBLEM == probInstance) {
                nqueen.randomRestartHillClibingUsingAttackingQueenHeuristic(allowedNumberOfRandomRestart, numberOfQueen);
            } else if(BONUSPROBLEM == probInstance) {
                nqueen.randomRestartHillClibingUsingMinConflictsHeuristic(allowedNumberOfRandomRestart, numberOfQueen);
            } else {
                System.out.println("Invalid Problem Instance selected");
            }
        }
        in.close();
    }
    /**
     * Constructor
     * @param none
     */
    NQueen() {
        this.totalRandomRestart = 0;
        this.totalStepInARestart = 0;
    }
    
    /**
     * Calculate random assignment of Queen board
     * @param numberOfQueen(Integer)
     * @return (Array)
     */
    private int[] createRandomAssignmentOfQueen(int numberOfQueen) {
        int[] queenPosition = new int[numberOfQueen + 1];
        Random rand = new Random();
        for(int i = 1; i <= numberOfQueen; i++) {
            queenPosition[i] = (numberOfQueen * rand.nextInt(numberOfQueen)) + i;
        }
        return queenPosition;
    }
    
    /**
     * For a given state, it calculates number of attacking(directly or indirectly) queen
     * @param queenPosition(Array)
     * @param numberOfQueen(Integer)
     * @return Integer
     */
    private int calculateNumberOfAttackingQueen(int[] queenPosition, int numberOfQueen) {
        int numberOfAttackingQueen = 0;
        for(int j = 1; j < numberOfQueen; j++) {
            int currentQueenPosition = queenPosition[j];
            int currentColumn =  ((currentQueenPosition % numberOfQueen) == 0) 
                    ? numberOfQueen :  (currentQueenPosition % numberOfQueen);
            int currentRow = ((currentQueenPosition % numberOfQueen) != 0) ? (
                    ((int) (currentQueenPosition / numberOfQueen)) + 1) 
                    : ((int) (currentQueenPosition / numberOfQueen));
            
            for(int i = currentColumn + 1; i <= numberOfQueen; i++) {
                int iThQueenColumn = ((queenPosition[i] % numberOfQueen) == 0) 
                    ? numberOfQueen :  (queenPosition[i] % numberOfQueen);
                int iThQueenRow = ((queenPosition[i] % numberOfQueen) != 0) ? (
                    ((int) (queenPosition[i] / numberOfQueen)) + 1) 
                        : ((int) (queenPosition[i] / numberOfQueen));
                if(currentRow == iThQueenRow) {
                    numberOfAttackingQueen++; 
                } else if(Math.abs(currentRow - iThQueenRow) 
                        == Math.abs(currentColumn - iThQueenColumn)) {
                    numberOfAttackingQueen++;
                }
            }
        }
        return numberOfAttackingQueen;
    }
    
    /**
     * Calculate the next queen board state by finding out the minimum attacking queen among n*n state
     * For multiple minimum attacking queen, it takes the last calculated one
     * @param currentQueenPosition
     * @param numberOfQueen
     * @return 
     */
    private int[] calculateNextBoardStateUsingAttakingQueenHeuristic(int[] currentQueenPosition, int numberOfQueen) {
        int[] nextState = new int[numberOfQueen + 1];
        int[] finalState = new int[numberOfQueen + 1];
        int currentNumberOfAttackingQueen = this.calculateNumberOfAttackingQueen(currentQueenPosition, numberOfQueen);
        int numberOfNextStateAttackingQueen = currentNumberOfAttackingQueen;
        boolean isStateChanged = false;
        for(int i = 1; i <= numberOfQueen; i++) {
            System.arraycopy(currentQueenPosition, 0, nextState, 0, currentQueenPosition.length);
            for(int j = 1; j <= numberOfQueen; j++) {
                if(nextState[i] == (((j - 1) * numberOfQueen) + i)) {
                    continue;
                }
                nextState[i] = ((j - 1) * numberOfQueen) + i;
                numberOfNextStateAttackingQueen = this.calculateNumberOfAttackingQueen(nextState, numberOfQueen);
                if(numberOfNextStateAttackingQueen <= currentNumberOfAttackingQueen) {
                    isStateChanged = true;
                    System.arraycopy(nextState, 0, finalState, 0, nextState.length);
                    currentNumberOfAttackingQueen = numberOfNextStateAttackingQueen;
                }
            }
        }
        if(!isStateChanged) {
            finalState = null;
        }
        return finalState;
    }
    
    /**
     * This method will find non-conflicting board position of queen based on Random Restart Hill climbing and Minimum number of attacking queen
     * This algorithm allows 200 sideway movement of the queen to tackle shoulder or flat minimum. If it fails, it restart the problem instance by randomly initializing
     * initial board position
     * @param numberOfRestart(Integer)
     * @param numberOfQueen(Integer)
     */
    public void randomRestartHillClibingUsingAttackingQueenHeuristic(int numberOfRestart, int numberOfQueen) {
        long startTime = System.currentTimeMillis();
        int[] queenPosition = null;
        int currentAttackingQueen = -1;
        int nextAttackingQueen = -1;
        int countSideWayMove = 0;
        int totalStateVisited = 0;
        boolean solutionFound = false;
        this.totalRandomRestart = 0;
        this.totalStepInARestart = 0;
        while(numberOfRestart != 0) {
            numberOfRestart--;
            queenPosition = this.createRandomAssignmentOfQueen(numberOfQueen);
            System.out.println("Initial Assignment, RandomRestart : " + (allowedNumberOfRandomRestart - numberOfRestart - 1));
            this.printQueenBoard(queenPosition, numberOfQueen);
            
            currentAttackingQueen = this.calculateNumberOfAttackingQueen(queenPosition, numberOfQueen);
            this.totalRandomRestart++;
            totalStateVisited += totalStepInARestart;
            this.totalStepInARestart = 0;
            while(true) {
                if(currentAttackingQueen == 0) {
                    long endTime = System.currentTimeMillis();
                    totalStateVisited += totalStepInARestart;
                    System.out.println("Solution found in " + ((double)(endTime - startTime) / 1000) + "seconds.");
                    System.out.println("Total State Visited: " + totalStateVisited);
                    System.out.println("Total Random Restart: " + (this.totalRandomRestart - 1));
                    System.out.println("Final Solution, After Random Restart: " + (allowedNumberOfRandomRestart - numberOfRestart - 1)); 
                    printQueenBoard(queenPosition, numberOfQueen);
                    System.out.println("\n");
                    solutionFound = true;
                    return;
                }
                
                int[] nextState = this.calculateNextBoardStateUsingAttakingQueenHeuristic(queenPosition, numberOfQueen);
                if(nextState == null) {
                    break;
                }
                
                this.totalStepInARestart++;
                
                nextAttackingQueen = this.calculateNumberOfAttackingQueen(nextState, numberOfQueen);
                if(nextAttackingQueen == currentAttackingQueen) {
                    countSideWayMove++;
                } else {
                    countSideWayMove = 0;
                    currentAttackingQueen = nextAttackingQueen;
                    System.arraycopy(nextState, 0, queenPosition, 0, nextState.length);
                }
                if(countSideWayMove > allowedSideWayMove) {
                    break;
                }
            }
        }
        if(!solutionFound) {
            System.out.println("No solution found after " + allowedNumberOfRandomRestart + " Random Restart");
        }
    }
    
    /**
     * Print the Queen Board
     * @param queenPosition(Array)
     * @param numberOfQueen(Integer)
     */
    private void printQueenBoard(int[] queenPosition, int numberOfQueen) {
        int[] temp = new int[numberOfQueen + 1];
        System.arraycopy(queenPosition, 0, temp, 0, queenPosition.length);
        Arrays.sort(temp, 1, numberOfQueen + 1);
        int[] t = new int[numberOfQueen];
        for(int i = 1; i <= numberOfQueen; i++) {
            int currentQueenColumn = ((temp[i] % numberOfQueen) == 0) 
                        ? numberOfQueen :  (temp[i] % numberOfQueen);
            Arrays.fill(t, 0);
            t[currentQueenColumn - 1] = 1;
            System.out.println(Arrays.toString(t));
        }
    }
    
    /**
     * Calculate total conflicts for a specific queen
     * @param queenPosition(Integer)
     * @param currentQueenPosition(Array)
     * @param numberOfQueen(Integer)
     * @return Integer
     */
    private int calculateNumberOfConflictForAQueen(int[] queenPosition, int currentQueenPosition, int numberOfQueen) {
        int currentQueenColumn = ((currentQueenPosition % numberOfQueen) == 0) 
                    ? numberOfQueen :  (currentQueenPosition % numberOfQueen);
        int currentQueenRow = ((currentQueenPosition % numberOfQueen) != 0) ? (
                    ((int) (currentQueenPosition / numberOfQueen)) + 1) 
                    : ((int) (currentQueenPosition / numberOfQueen));
        int numberOfConflict = 0;
        for(int i = 1; i <= numberOfQueen; i++ ) {
            if(queenPosition[i] == currentQueenPosition) {
                continue;
            }
            
            int iThQueenColumn = ((queenPosition[i] % numberOfQueen) == 0) 
                ? numberOfQueen :  (queenPosition[i] % numberOfQueen);
            int iThQueenRow = ((queenPosition[i] % numberOfQueen) != 0) ? (
                ((int) (queenPosition[i] / numberOfQueen)) + 1) 
                    : ((int) (queenPosition[i] / numberOfQueen));
            if(currentQueenRow == iThQueenRow) { //Row Conflict
                numberOfConflict++; 
            } else if(Math.abs(currentQueenRow-iThQueenRow) //Diagonal conflict
                    == Math.abs(currentQueenColumn- iThQueenColumn)) {
                numberOfConflict++;
            }
        }
        return numberOfConflict;
    }
    
    /**
     * Calculate MinConflict for a queen in all position of a specific column
     * @param queenPosition(Array)
     * @param numberOfQueen(Integer)
     * @param column(Integer)
     */
    private void calculateMinConflictsForAColumn(int[] queenPosition, int numberOfQueen, int column) {
        int currentConflict = numberOfQueen * numberOfQueen;
        int tempRow = 0;
        for(int i = 1; i <= numberOfQueen; i++ ) {
            int c = this.calculateNumberOfConflictForAQueen(queenPosition, ((i - 1) * numberOfQueen) + column, numberOfQueen);
            if(c <= currentConflict) {
                currentConflict = c;
                tempRow = i;
            }
        }
        queenPosition[column] = (tempRow - 1) * numberOfQueen + column;
    }
    
    /**
     * MinConflict algorithm using max_step
     * it selects column randomly for 200 times and tries to place corresponding queen in a row with minimum conflict
     * @param queenPosition(Array)
     * @param numberOfQueen(Integer)
     * @return boolean
     */
    private boolean calculateMinConflictsForAllQueen(int[] queenPosition, int numberOfQueen) {
        boolean ret = false;
        Random rand = new Random();
        for(int i = 1; i <= this.max_step; i++) {
            this.totalStepInARestart++;
            if(this.calculateNumberOfAttackingQueen(queenPosition, numberOfQueen) == 0) {
                System.out.println("Total steps required in Min Conflict : " + i );
                ret = true;
                break;
            }
            this.calculateMinConflictsForAColumn(queenPosition, numberOfQueen, rand.nextInt(numberOfQueen + 1));
        }
        if(!ret && this.calculateNumberOfAttackingQueen(queenPosition, numberOfQueen) == 0) {
            System.out.println("Total steps required in Min Conflict : " + this.max_step );
            ret = true;
        }
        return ret;
    }
    
    /**
     * This method will find non-conflicting board position of queen based on Random Restart Hill climbing and Min-Conflicts Heuristic
     * The Min-Conflict algorithm uses at max 100 steps. If it fails, it restart the problem instance by randomly initializing
     * initial board position
     * @param numberOfRestart(Integer) 
     * @param numberOfQueen(Integer)
     */
    public void randomRestartHillClibingUsingMinConflictsHeuristic(int numberOfRestart, int numberOfQueen) {
        long startTime = System.currentTimeMillis();
        this.totalRandomRestart = 0;
        this.totalStepInARestart = 0;
        int totalStateVisited = 0;
        while(numberOfRestart != 0) {
            numberOfRestart--;
            this.totalRandomRestart++;
            totalStateVisited += totalStepInARestart;
            this.totalStepInARestart = 0;
            int[] queenPosition = this.createRandomAssignmentOfQueen(numberOfQueen);
            System.out.println("Initial Assignment, RandomRestart : " + (allowedNumberOfRandomRestart - numberOfRestart - 1));
            this.printQueenBoard(queenPosition, numberOfQueen);
            
            boolean ret = this.calculateMinConflictsForAllQueen(queenPosition, numberOfQueen);
            
            if(ret) {
                long endTime = System.currentTimeMillis();
                totalStateVisited += totalStepInARestart;
                System.out.println("Solution found in " + ((double)(endTime - startTime) / 1000) + "seconds.");
                System.out.println("Total State Visited: " + totalStateVisited);
                System.out.println("Total Random Restart: " + (this.totalRandomRestart - 1));
                System.out.println("Final Solution, After Random Restart: " + (allowedNumberOfRandomRestart - numberOfRestart - 1));
                this.printQueenBoard(queenPosition, numberOfQueen);
                System.out.println("\n");
                return;
            } else {
                System.out.println("Min conflict stuck after " + this.max_step + " steps"); 
                System.out.println("Stuck Queen Board Position");
                this.printQueenBoard(queenPosition, numberOfQueen);
                System.out.println("Random Restart " + (allowedNumberOfRandomRestart - numberOfRestart) + " is starting.");
            }
        }
    }
}
