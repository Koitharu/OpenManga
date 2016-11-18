package org.nv95.openmanga.components.reader.recyclerpager.overscroll;


/**
 * @author amit
 */
public interface ListenerStubs {

    class OverScrollStateListenerStub implements IOverScrollStateListener {
        @Override
        public void onOverScrollStateChange(IOverScrollDecor decor, int oldState, int newState) { }
    }

    class OverScrollUpdateListenerStub implements IOverScrollUpdateListener {
        @Override
        public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) { }
    }
}