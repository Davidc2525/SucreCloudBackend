package orchi.HHCloud.store.response;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class GetStatusResponse extends Response {
	private long size;
	@JsonIgnore
	private List<Path> paths;

	@JsonProperty(value = "paths")
	public List<String> getStringPaths() {
		List<String> toString = new ArrayList<>();
		if(paths!=null){
			paths.forEach(p -> {
				toString.add(p.toString());
			});	
		}
		return toString;
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
