import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

class ObservePhilosopher implements Runnable{
    int T_observe;
    Diner.Philosopher philosopher;

    Diner.Table table;
    ObservePhilosopher(Diner.Philosopher philosopher, int T_observ, Diner.Table table){
        this.philosopher = philosopher;
        this.T_observe = T_observ;
        this.table = table;
    }

    @Override
    public void run(){
        while(philosopher.getAlive()){

            try{
                Thread.sleep(T_observe);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            synchronized(table) {
                table.addInfo(philosopher.getName(), philosopher.getInfo());
            }
        }
    }
}

class Dinnering implements Runnable{

    int philosopherName;
    Diner.Table table;
    Semaphore sem;

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

        ObservePhilosopher observPhilosopher = new ObservePhilosopher(philosopher, T_observe, table);
        Thread thredObserv = new Thread(observPhilosopher);
        thredObserv.start();

        System.out.println("Гость(" + philosopher.getName() + "): присоединился к трапизе");

        try {
            while (!Thread.currentThread().isInterrupted()) {

                synchronized (table.fork[philosopher.getName()]) {
                    if (philosopher.getName() < table.N_Fork - 1) {
                        synchronized (table.fork[philosopher.getName() + 1]) {
                            philosopher.eat();

                            philosopher.thinks();
                        }
                    } else {
                        synchronized (table.fork[0]) {
                            philosopher.eat();

                            philosopher.thinks();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        philosopher.setAlive(false);
    }
}

public class Diner {

    static final int N = 5;
    static final int T_dinner = 3000;
    static final int T_observe = 100;
    static final int t_eat = 0;
    static final int t_think = 0;


    static class Philosopher {
        private boolean eat;
        private boolean think;
        private int name;
        private boolean alive;
        private int wasEaten=0;
        private long allTime;
        private long nowTime;
        private long startTime;
        private Boolean firstTime;

        Philosopher(int name) {
            this.name = name;
            alive = true;
            eat = false;
            think = false;
            allTime = 0;
            nowTime = 0;
            startTime = 0;
            firstTime = true;
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
        public void eat() throws InterruptedException {
            if(!firstTime) {
                nowTime = (int) (System.currentTimeMillis() - startTime);
            }
            allTime += nowTime;
            eat = true;

            wasEaten++;
            Thread.sleep(t_eat);

            eat = false;
        }

        public void thinks() throws InterruptedException {

            think = true;

            Thread.sleep(t_think);

            think = false;
            startTime = System.currentTimeMillis();
            firstTime = false;
        }

        public String getInfo() {
            String info;
            info = "Гость("+name+"):\n"+
                    "Кушает: "+eat+
                    "\nДумает: "+think+
                    "\nПокушал и подумал раз: "+wasEaten+
                    "\nПоследние время ожидания: "+nowTime+
                    "\nВремяни на всё: "+allTime;
            return info;
        }
    }

    static class Table{
        static class GuestsInfo {
            private String info;

            public void setInfo(String info){
                this.info = info;
            }
            public String getInfo(){return info;}
        }

        public static Object []fork = new Object[N];
        public int N_Fork;

        private GuestsInfo []guestsInfo = new GuestsInfo[N];
        private int infoInt;

        Table() {
            infoInt = 0;

            N_Fork = fork.length;
            for(int i=0; i<N_Fork; i++){
                fork[i] = new Object();
                guestsInfo[i] = new GuestsInfo();

            }
        }
        public void addInfo(int guestId,String info){
            infoInt++;
            guestsInfo[guestId].setInfo(info);

            if(infoInt == N){
                System.out.println("||============INFO============||");
                System.out.println("Информация про гостей:");
                for(int i=0; i<N; i++){
                    System.out.println(guestsInfo[i].getInfo());
                }
                infoInt = 0;
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
            for (int i=0; i<N;i++){
                chair[i].interrupt();
            }
        }  catch(InterruptedException e){

        }
    }

}
