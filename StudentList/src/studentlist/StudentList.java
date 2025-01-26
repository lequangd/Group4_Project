/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package studentlist;

/**
 *
 * @author sivagamasrinivasan
 */
public class StudentList {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //user interaction
        // define 3 students and print 
        Student[] list = new Student[3]; // sid, name 
        //array of object declaration
        //create object
        Student s1 = new Student();
        s1.setName("peter");
        s1.setSid(1);
         Student s2 = new Student();
        s2.setName("tarun");
        s2.setSid(2);
         Student s3 = new Student();
        s3.setName("john");
        s3.setSid(3);
       // s1,s2,s3 - saved the object in array
       list[0] = s1;
       list[1]=s2;
       list[2]=s3;
       for(int i=0;i<list.length;i++)
       {
          System.out.println(list[i].getName()+ " "+list[i].getSid());
       }
        
    }
    
}
