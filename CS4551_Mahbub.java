import java.util.Scanner;

public class CS4551_Mahbub{
  public static void main(String[] args){
    System.out.println("--Welcome to Multimedia Software System--");
    Scanner input = new Scanner(System.in);
    int option = 0;
    boolean running = true;

    while(running){
      System.out.println("Main Menu-----------------------------------");
      System.out.println("1. Block-Based Motion COmpensation");
      System.out.println("2. Removing Moving Objects");
      System.out.println("3. Quit");
      System.out.println("Please enter the task number [1-3]:");
      option = input.nextInt();
      
      if(option == 1){
        System.out.print("Enter Target Frame(three digits):");
        int targetFrame = input.nextInt();
        
        System.out.print("Enter Reference(three digits):");
        int referenceFrame = input.nextInt();

        System.out.print("Enter macro-block size (n):");
        int n = input.nextInt();

        System.out.print("Enter search window (p):");
        int p = input.nextInt();

        MotionCompensation motion = new MotionCompensation(targetFrame, referenceFrame, n, p);
        motion.taskOne();
      }
      else if(option == 2){
        System.out.print("Enter Target Frame(three digits):");
        int targetFrame = input.nextInt();
        
        System.out.print("Enter macro-block size (n):");
        int n = input.nextInt();

        System.out.print("Enter search window (p):");
        int p = input.nextInt();

        MotionCompensation motion = new MotionCompensation(targetFrame, n, p);
        motion.taskTwo();
      }
      else if(option == 3){
        running = false;
      }
      else{
        running = false;
      }  
    }
    input.close();
    System.out.println("--Good Bye--");
    System.exit(0);
  }  
}
