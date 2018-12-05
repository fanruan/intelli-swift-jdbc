package com.fr.swift.transacion;

import com.fr.swift.transaction.TransactionManager;
import com.fr.swift.transaction.TransactionProxyFactory;
import com.fr.third.springframework.web.bind.annotation.ControllerAdvice;
import junit.framework.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class created on 2018/6/26
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
@ControllerAdvice
public class TransatcionTest extends TestCase {
    public void testRollBack() {
        try {
            final BlockingQueue queue = new LinkedBlockingQueue();
            queue.put("123");
            TransactionProxyFactory proxyFactory = new TransactionProxyFactory(new TransactionManager() {
                @Override
                public void setOldAttach(Object o) {

                }

                @Override
                public void start() {

                }

                @Override
                public void commit() {

                }

                @Override
                public void rollback() {
                    assertEquals(queue.poll(), "123");
                }

                @Override
                public void close() {

                }
            });

            ITransactionTestBean testBean = (ITransactionTestBean) proxyFactory.getProxy(new TransactionTestBean());
            assertEquals(3, testBean.add(1, 2));
            try {
                testBean.divided(10);
            } catch (Exception e) {
                assertTrue(true);
            }
            assertTrue(queue.poll() == null);
        } catch (InterruptedException e) {
            assertTrue(false);
        }
    }
}
