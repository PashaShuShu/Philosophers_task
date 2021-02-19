import java.lang.Thread;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

class ObservePhilosopher implements Runnable{
    int T_observe;
    Diner.Philosopher philosopher;
    CyclicBarrier barrier;
    ObservePhilosopher(Diner.Philosopher philosopher, int T_observ, CyclicBarrier barrier){
        this.philosopher = philosopher;
        this.T_observe = T_observ;
        this.barrier = barrier;
    }

    @Override
    public void run(){
        /*try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }*/
        try{
            Thread.sleep(50);
        }catch(InterruptedException e){}
        while(philosopher.getAlive()){
            System.out.println(philosopher.getInfo());
            try{
                Thread.sleep(T_observe);
            }catch(InterruptedException e){}
        }
    }
}

class Dinnering implements Runnable{

    int philosopherName;
    Diner.Table table;
    Semaphore sem;
    int updatableTime;
    int t_dinner;
    int T_observe;
    CyclicBarrier barrier;

    Dinnering(int philosopherName, Diner.Table table, Semaphore sem, int t_dinner, int T_observe, CyclicBarrier barrier){
        this.philosopherName = philosopherName;
        this.table = table;
        this.sem = sem;
        this.t_dinner = t_dinner;
        this.T_observe = T_observe;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        Diner.Philosopher philosopher = new Diner.Philosopher(philosopherName);

        ObservePhilosopher observPhilosopher = new ObservePhilosopher(philosopher, T_observe, barrier);
        Thread thredObserv = new Thread(observPhilosopher);
        thredObserv.start();

        System.out.println("Гость("+philosopher.getName()+"): присоединился к трапизе");

        boolean forkRight;
        boolean forkLeft;
        long nowTime = System.currentTimeMillis();
        try{
            while(updatableTime<t_dinner){
                synchronized(table) {
                    synchronized(table.fork[philosopher.getName()]){
                        if (philosopher.getName() < table.N_Fork - 1) {
                            synchronized(table.fork[philosopher.getName()+1]){
                                philosopher.eat();
                                philosopher.thinks();
                            }
                        } else {
                            synchronized(table.fork[0]){
                                philosopher.eat();
                                philosopher.thinks();
                            }
                        }
                    }
                }
                updatableTime = (int) (System.currentTimeMillis() - nowTime);
            }
        }
        catch(Exception e){
            System.out.println("что-то пошло не так: "+e);
        }
        philosopher.setAlive(false);
        System.out.println(updatableTime);
    }
}

public class Diner {

    static final int N = 3;
    static final int T_dinner = 3000;
    static final int T_observe = 500;
    static final int t_eat = 1;
    static final int t_think = 1;


    static class Philosopher {
        private boolean rightHend;
        private boolean leftHend;
        private boolean eat;
        private boolean think;
        private int name;
        private boolean alive;
        private int wasEaten=0;


        Philosopher(int name) {
            this.name = name;
            rightHend = false;
            leftHend = false;
            alive = true;
            eat = false;
            think = false;
        }

        public void setAlive(boolean dead){
            alive = dead;
        }

        public boolean getAlive(){
            return alive;
        }

        public int getName(){
            return name;
        }

        public boolean getRightHend() {
            return rightHend;
        }

        public boolean getLeftHend() {
            return leftHend;
        }

        public void setRightHend(boolean fork) {
            rightHend = fork;
        }

        public void setLeftHend(boolean fork) {
            leftHend = fork;
        }

        public void eat() {
            eat = true;
            wasEaten++;
            try {
                Thread.sleep(t_eat);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            eat = false;
        }

        public void thinks() {
            think = true;
            try {
                Thread.sleep(t_think);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            think = false;
        }

        public String getInfo() {
            String info;
            info = "Гость("+name+"):\n"+
                    "Кушает: "+eat+
                    "\nДумает: "+think+
                    "\nПокушал и подумал раз: "+wasEaten;
            return info;
        }
    }

    static class Table{

        public static Object []fork = new Object[N];
        int N_Fork;
        Table() {
            N_Fork = fork.length;
            for(int i=0; i<N_Fork; i++){
                fork[i] = "";
            }
        }

    }

    public static void main(String args[]){
        System.out.println("hello diner");
        Thread []chair = new Thread[N];
        Table table = new Table();
        Semaphore sem = new Semaphore(N/2);
        CyclicBarrier barrier = new CyclicBarrier(N);
        for (int i=0; i<N;i++){
            Dinnering dinnering = new Dinnering(i, table, sem,T_dinner,T_observe,barrier);
            chair[i] = new Thread(dinnering);
            chair[i].start();
        }
        try {
            Thread.sleep(T_dinner);
            //chair[4].interrupt();

        }  catch(InterruptedException e){

        }
    }

}
