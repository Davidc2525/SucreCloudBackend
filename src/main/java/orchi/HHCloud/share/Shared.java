/**
 * Shared.java
 */
package orchi.HHCloud.share;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author david 14 ago. 2018
 */
public class Shared implements Externalizable {
	private List<Share> shared = new ArrayList<>();

	public List<Share> getShared() {
		return shared;
	}

	public void setShared(List<Share> shared) {
		this.shared = shared;
	}
	
	public void addShare(Share share){
		shared.add(share);
	}
	
	public boolean isShared(Path path){
		boolean isShared = false;
		Iterator<Share> iter = shared.iterator();
		while(iter.hasNext()){
			Share share = iter.next();
			if(share.getPath().toString().equals(path.toString())){
				isShared = true;
			}
		}
		return isShared;
	};

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setShared((List<Share>)in.readObject());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(getShared());
	}

	@Override
	public String toString() {
		return "Shared {shared=" + shared + "}";
	}
	
}
