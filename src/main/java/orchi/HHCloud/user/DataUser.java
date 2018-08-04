package orchi.HHCloud.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DataUser extends BasicUser {

	private String firstName;
	private String lastName;
	private Long createAt;
	private boolean emailVerified = false;

	public User bind(String id, String username, String email, boolean emailVerified,String password, String firstName, String lastName,Long createAt) {
		
		super.bind(id, username, email, password);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setCreateAt(createAt);
		this.setEmailVerified(emailVerified);
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(getFirstName());
		out.writeUTF(getLastName());
		out.writeLong(getCreateAt());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		setFirstName(in.readUTF());
		setLastName(in.readUTF());
		setCreateAt(in.readLong());
	}

	/**
	 * @return the createAt
	 */
	public Long getCreateAt() {
		return createAt;
	}

	/**
	 * @param createAt
	 *            the createAt to set
	 */
	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	/**
	 * @return the emailVerified
	 */
	public boolean isEmailVerified() {
		return emailVerified;
	}

	/**
	 * @param emailVerified the emailVerified to set
	 */
	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	@Override
	public String toString() {
		return "DataUser {id=" + id + ", username=" + username + ", email=" + email + ", password=" + password
				+ ", firstName=" + firstName + ", lastName=" + lastName + ", createAt=" + createAt + ", emailVerified="
				+ emailVerified + "}";
	}


}
