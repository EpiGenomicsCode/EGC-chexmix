package org.egc.core.viz.metaprofile;

import java.util.*;

import org.egc.core.genome.location.Point;


public class MetaProfileHandler<T extends Point, ProfileClass extends Profile> {

	private MetaProfile profile;
	private PointProfiler<T, ProfileClass> profiler, threadSafe;
	private Vector<PointAddingThread> currentlyAdding;
	private List<Thread> activeThreads = new ArrayList<Thread>();
	
	public MetaProfileHandler(String name, BinningParameters bps, PointProfiler<T,ProfileClass> pp, boolean normalizedMeta) { 
		if(normalizedMeta)
			profile = new NormalizedMetaProfile(name, bps);
		else
			profile = new MetaProfile(name, bps);		
		profiler = pp;
		threadSafe = new ThreadSafeProfiler();
		currentlyAdding = new Vector<PointAddingThread>();
	}
	
	public MetaProfile getProfile() { return profile; }
	
	public void addPoints(Collection<T> points) { 
		addPoints(points.iterator());
	}
	
	public void addPoints(Iterator<T> points) { 
		PointAddingThread pat = new PointAddingThread(points);
		startAddingThread(pat);
	}
	
	private void startAddingThread(PointAddingThread pat) { 
		synchronized(currentlyAdding) { 
			currentlyAdding.add(pat);
			Thread t = new Thread(pat);
			activeThreads.add(t);
			t.start();		
		}
	}
	
	private void addingThreadFinished(PointAddingThread pat) { 
		synchronized(currentlyAdding) { 
			currentlyAdding.remove(pat);
		}
	}
	
	/**
	 * Block until all point-adding threads have completed.
	 */
	public void awaitCompletion() {
		for(Thread t : activeThreads) {
			try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}
		activeThreads.clear();
	}
	
	private class ThreadSafeProfiler implements PointProfiler<T,ProfileClass> {

		public BinningParameters getBinningParameters() {
			return profiler.getBinningParameters();
		}

		public ProfileClass execute(T a) {
			synchronized(this) { 
				return profiler.execute(a);
			}
		} 	
		public void cleanup() {}
	}
	
	private class PointAddingThread implements Runnable { 
		
		public boolean running;
		private Iterator<T> points;

		public PointAddingThread(Iterator<T> pts) { 
			running = true;
			points = pts;
		}
		
		public void stopAdding() { 
			running = false;
		}
		
		public void run() { 
			while(running && points.hasNext()) { 
				T pt = points.next();
				profile.addProfile(threadSafe.execute(pt));
			}
			running=false;
			addingThreadFinished(this);
		}
	}
}
