package repairencoding.types;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Util for easy multithread
 * @author Aleksey K. (PlayerO1)
 */
public class TUtil {
    public static int MT_THREAD_COUNT = 1; // use single thread or use parallel process
    
    /**
     * Create thread pool
     * @param name Name of thread pool. Using for VisualVM or other debugging
     * @param items number of processing items, or 0 for ignore this parameter
     * @return 
     */
    public static ExecutorService newExecutor(String name, int items) {
        if (name==null) name="thread_poll";
        int tnum = items<1 || items>MT_THREAD_COUNT? MT_THREAD_COUNT : items; // Modern CPU has many cores, more that you need :P
        return Executors.newFixedThreadPool(tnum, new NamedTFactory(name));
    }

    public static class NamedTFactory implements ThreadFactory {
        protected final String group;
        protected int n=0;

        public NamedTFactory(String group) {
            this.group = group;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t=new Thread(r, group+"-"+(n++));
            t.setDaemon(true);
            return t;
        }
        
    }
}
