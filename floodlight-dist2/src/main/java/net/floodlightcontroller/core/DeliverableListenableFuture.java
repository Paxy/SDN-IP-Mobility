package net.floodlightcontroller.core;

import com.google.common.util.concurrent.AbstractFuture;

/** Implementation of a ListenableFuture that provides a Deliverable interface to
 *  the provider.
 *
 * @author Andreas Wundsam <andreas.wundsam@bigswitch.com>
 * @see Deliverable
 * @param <T>
 */
public class DeliverableListenableFuture<T> extends AbstractFuture<T> implements Deliverable<T> {
    
    public void deliver(final T result) {
        set(result);
    }

    
    public void deliverError(final Throwable cause) {
        setException(cause);
    }
}
