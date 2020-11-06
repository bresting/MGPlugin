package test;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2020.08.05   KIM_DO_JIN         최초생성
 *
 * </pre>
 */
public class Progress {
    
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        IRunnableWithProgress op = new CopyThread(10);
        new ProgressMonitorDialog(new Shell()).run(true, true, op);;
    }
    
    private static class CopyThread implements IRunnableWithProgress {
        
        private int workLoad = 0;
        public CopyThread(int workLoad) {
            // 생성자
            this.workLoad = workLoad;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("데이터복사", workLoad);
            
            // 작업코드
            for (int i = 0; i < workLoad; i++) {
                monitor.subTask("복사: " + i + " / " + workLoad);
                Thread.sleep(1000);
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    monitor.done();
                    return;
                }
            }
            monitor.done();
        }
    }
}
