import java.lang.Thread;
import java.util.concurrent.Semaphore;

class ObservPhilosopher implements Runnable{
    int T_observ;
    Diner.Philosopher philosopher;
    ObservPhilosopher(Diner.Philosopher philosopher, int T_observ){
        this.philosopher = philosopher;
        this.T_observ = T_observ;
    }

    @Override
    public void run(){
        try{
            Thread.sleep(50);
        }catch(InterruptedException e){}
        while(philosopher.getAlive()){
            System.out.println(philosopher.getInfo());
            try{
                Thread.sleep(T_observ);
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
    int T_observ;

    Dinnering(int philosopherName, Diner.Table table, Semaphore sem, int t_dinner, int T_observ){
        this.philosopherName = philosopherName;
        this.table = table;
        this.sem = sem;
        this.t_dinner = t_dinner;
        this.T_observ = T_observ;
    }

    @Override
    public void run() {
        Diner.Philosopher philosopher = new Diner.Philosopher(philosopherName);

        ObservPhilosopher observPhilosopher = new ObservPhilosopher(philosopher,T_observ);
        Thread thredObserv = new Thread(observPhilosopher);
        thredObserv.start();

        System.out.println("Гость("+philosopher.getName()+"): присоединился к трапизе");

        boolean forkRight;
        boolean forkLeft;
        long nowTime = System.currentTimeMillis();

        while(updatableTime<t_dinner){

            try{
                synchronized(table) {
                    //Semaphore on
                    //sem.acquire();
                    forkLeft = table.getFork(philosopher.getName());
                    //System.out.println("Гость(" + philosopher.getName() + "): левая вилка- " + forkLeft);

                    if (philosopher.getName() < table.N_Fork - 1) {
                        forkRight = table.getFork(philosopher.getName() + 1);
                    } else {
                        forkRight = table.getFork(0);
                    }

                    //.out.println("Гость(" + philosopher.getName() + "): правая вилка- " + forkRight);

                    if (forkLeft && forkRight) {

                        //System.out.println("Гость(" + philosopher.getName() + "): кушает");
                        philosopher.eat();
                        //System.out.println("Гость(" + philosopher.getName() + "): поел");

                        table.putForkBack(philosopher.getName());
                        if (philosopher.getName() < table.N_Fork - 1) {
                            table.putForkBack(philosopher.getName() + 1);
                        } else {
                            table.putForkBack(0);
                        }
                        //Semaphore off
                        //sem.release();

                        //System.out.println("Гость(" + philosopher.getName() + "): филосовствует");
                        philosopher.thinks();

                    } else {
                        if (forkLeft) {
                            table.putForkBack(philosopher.getName());
                        }
                        if (forkRight) {
                            if (philosopher.getName() < table.N_Fork - 1) {
                                table.putForkBack(philosopher.getName() + 1);
                            } else {
                                table.putForkBack(0);
                            }
                        }
                    }
                }
            }catch(Exception e){
                System.out.println ("У гостя(" + philosopher.getName() + "): проблемы со здоровьем");
            }

            updatableTime = (int) (System.currentTimeMillis() - nowTime);
        }
        philosopher.setAlive(false);
        //System.out.println(updatableTime);
    }
}

public class Diner {

    static final int N = 5;
    static final int T_dinner = 4000;
    static final int T_observ = 1000;
    static final int t_eat = 100;
    static final int t_think = 100;


    static class Philosopher {
        private boolean rightHend;
        private boolean leftHend;
        private boolean eat;
        private boolean think;
        private int name;
        private boolean alive;


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
                    "\nДумает: "+think;
            return info;
        }
    }

    static class Table{
        private boolean []fork = new boolean[N];
        int N_Fork;
        Table() {
            for (int i = 0;i<N;i++){
                fork[i] = true;
            }
            N_Fork = fork.length;
        }
        public boolean getFork(int index){
             if(fork[index]==false){
                 return false;
             }else{
                 fork[index] = false;
                 return true;
             }
        }
        public void putForkBack(int index){
            fork[index]=true;
        }
    }

    public static void main(String args[]){
        System.out.println("hello diner");
        Thread []chair = new Thread[N];
        Table table = new Table();
        Semaphore sem = new Semaphore(N/2);
        for (int i=0; i<N;i++){
            Dinnering dinnering = new Dinnering(i, table, sem,T_dinner,T_observ);
            chair[i] = new Thread(dinnering);
            chair[i].start();
        }
        try {
            Thread.sleep(T_dinner);


        }  catch(InterruptedException e){

        }
    }

}
