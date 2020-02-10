package task;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScanTask {

    private  final ExecutorService pool = Executors.newFixedThreadPool(3);

    //private static volatile int COUNT;

    //使用AtomicInteger可以保证线程安全；不用synchronized的原因是考虑性能问题
    //原子性的操作；java.util.current.actomic包下的类
    private final AtomicInteger count = new AtomicInteger();

    //方式一
    //闭锁：计数器 调用await的线程阻塞等待（一个线程），直到计数器=0；
    //latch.countDown()计数减一
    //latch.await()阻塞等待直到计数器=0；然后继续往下执行
    private final CountDownLatch latch = new CountDownLatch(1);

    //方式二
    /*
    栅栏：所有线程一起等待，直到计数器=0；
    await():代表所有线程都阻塞等待,计数器减一直到所有计数器=0；然后所有线程往下执行
        private static final CyclicBarrier BARRIER = new CyclicBarrier(3);
     */

    //方式三
    /*
    信号量：
    计数器：初始值
    计数器增加：
    semaphore.release();+1
     semaphore.release(num);+num
     申请计数器一定数量的资源,申请不到就阻塞等待
      semaphore.acquire();申请的资源数-1
       semaphore.acquire();申请的资源数-num
       private static final Semaphore SEMAPHORE = new Semaphore(1);
     */

    private FileScanCallback callback;

    public FileScanTask(FileScanCallback callback) {
        this.callback = callback;
    }

    //启动根目录的扫描任务
    public void startScan(File root) {
//        synchronized (this) {
//            COUNT++;
//        }
        count.incrementAndGet();
       pool.execute(new Runnable() {
           @Override
           public void run() {
               list(root);
           }
       });
    }

    public void list(File dir) {
        if (!Thread.interrupted()) {
            try {
                callback.execute(dir);
//                System.out.println(dir.getPath());
                if (dir.isDirectory()) {
                    File[] children = dir.listFiles();
                    if (children != null && children.length > 0) {
                        for (File child : children) {
                            //启动子线程执行子文件夹的扫描任务
                            if (child.isDirectory()) {
//                            synchronized (this) {
//                                COUNT++;
//                            }
                                count.incrementAndGet();
                                pool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        list(child);
                                    }
                                });
                            } else {
                                callback.execute(child);
 //                               System.out.println(child.getPath());
                            }
                        }
                    }
                }
            } finally {
//            synchronized (this) {
//                COUNT--;
//                if (COUNT == 0) {
//                    this.notifyAll();
//                }
//            }
                if (count.decrementAndGet() == 0) {
                    //通知
                    latch.countDown();
                }
            }
        }
    }

    //等待所有扫描任务执行完毕
    public void waitFinish() throws InterruptedException {
//        try {
//            synchronized (this) {
//                this.wait();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        try {
            latch.await();
        } finally {
            //中断所有线程
            pool.shutdown();//调用每个线程的interrupt()中断
//            POOL.shutdownNow();//调用每个线程的stop()关闭
        }
    }

//    public static void main(String[] args) throws InterruptedException {
//        FileScanTask task = new FileScanTask();
//        task.startScan(new File("F:\\javacode\\.idea"));
//        synchronized (task) {
//            task.wait();
//        }
//        System.out.println("over");
//    }


}
