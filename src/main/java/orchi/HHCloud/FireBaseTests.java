package orchi.HHCloud;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.apache.commons.fileupload.util.Streams;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class FireBaseTests {
    private static FirebaseAuth auth;
    private static FirebaseApp app;

    public static void main(String[] args) throws IOException, FirebaseAuthException {
        System.out.println(Streams.asString(Thread.class.getResource("/serviceAccountKey.json").openStream()));

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(
                        GoogleCredentials.fromStream(Thread.class.getResource("/serviceAccountKey.json").openStream()))
                .setDatabaseUrl("https://hhcloud-29d6a.firebaseio.com/").build();

        app = FirebaseApp.initializeApp(options);
        auth = FirebaseAuth.getInstance(app);

        DatabaseReference ref = FirebaseDatabase.getInstance(app).getReference();


        DatabaseReference usersRef = ref.child("users");

        Map<String, User> users = new HashMap<>();
        users.put("alanisawesome", new User("June 23, 1912", "Alan Turing"));
        users.put("gracehop", new User("December 9, 1906", "Grace Hopper"));

        usersRef.setValueAsync(users);

        String idToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImI4OWY3MzQ2YTA5ODVmNDIxZGNkOGQzMGMwYjMwZWViZmFlMTlhMWUifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vaGhjbG91ZC0yOWQ2YSIsImF1ZCI6ImhoY2xvdWQtMjlkNmEiLCJhdXRoX3RpbWUiOjE1MzI1MzQ0ODgsInVzZXJfaWQiOiJDTUVqVTZROHdOVEhMbko4clRRb2IwcmZuVUYzIiwic3ViIjoiQ01FalU2UTh3TlRITG5KOHJUUW9iMHJmblVGMyIsImlhdCI6MTUzMjUzNTM1MSwiZXhwIjoxNTMyNTM4OTUxLCJlbWFpbCI6Imx1aXNhQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJsdWlzYUBnbWFpbC5jb20iXX0sInNpZ25faW5fcHJvdmlkZXIiOiJwYXNzd29yZCJ9fQ.hCGRhqIrDz_HVomJEIh7ifcraWfJkw_f1bTqLP96L-FktxP-NTyCymav7Gklq3rDNug9QzfLjEOCZSAmkw82BENEZCslQmvR1azqBFG2jzU74XklQQzveDEuKJXYXnjWIPnPDefnRtJb2AA-FeOE840zHH9k8VjFPqN32JoRs18OVYiXiEjmEZTnYkw7AgkD6QPl9PN-C1Xq0AyePQwzQvt5zlE10ebU1aMc1b3iTaY2n4A7F0P4vFneNAaGyEM6Xw8XsMJ41M8IyQZ2vQXLp1lTqQyHFqyEkFmQZA2-0UcfMcwVzmbmCrevduLVnGbsiyOZZQn2ukk7YByfvwubgw";
        /* FirebaseAuth.getInstance().verifySessionCookie("eyJhbGciOiJSUzI1NiIsImtpZCI6IlFEQXl2QSJ9.eyJpc3MiOiJodHRwczovL3Nlc3Npb24uZmlyZWJhc2UuZ29vZ2xlLmNvbS9oaGNsb3VkLTI5ZDZhIiwiYXVkIjoiaGhjbG91ZC0yOWQ2YSIsImF1dGhfdGltZSI6MTUzMjUzMTYxNSwidXNlcl9pZCI6IkNNRWpVNlE4d05USExuSjhyVFFvYjByZm5VRjMiLCJzdWIiOiJDTUVqVTZROHdOVEhMbko4clRRb2IwcmZuVUYzIiwiaWF0IjoxNTMyNTMzNjUxLCJleHAiOjE1MzI1MzQwMTEsImVtYWlsIjoibHVpc2FAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJmaXJlYmFzZSI6eyJpZGVudGl0aWVzIjp7ImVtYWlsIjpbImx1aXNhQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6InBhc3N3b3JkIn19.OciyU9Gk2DfrJlqzG2x6CbGneYBMAJllh0bUZ5iSEOFtaPUzgddcN-wnmjVULjBxSIo5v9adTfYGq_uFX8mRiEMIeFWpLXMpzVb7mKdN90GLLRTu1w03jjcbVU-ABCpRH13QHLa8TPDP1e0juw4sKKcchefhtujqh2kRnxrbwzzs5vOXyPXrMwyQPPGm2xcVOvdfYcAfv1133BcTM8VTB-OV59f080T0I8BGNzYblYrNYJ-q3Le0B-ytgou5nHvHELDxZRsVi432IcNYqBTuILkXYVScHlRVx7cPTO59kjpSvOtJZsYLZo43mrKMPbKWHYI6uLtdvzfi41ywGQGcdQ",true);
		 
		 
		long expiresIn = TimeUnit.MINUTES.toMillis(6);
		  SessionCookieOptions optionsc = SessionCookieOptions.builder()
		      .setExpiresIn(expiresIn)
		      .build();
		System.out.println(auth.createSessionCookie(idToken, optionsc));;
		
		FirebaseToken decodedToken = auth.verifyIdToken(idToken,true);
		String uid = decodedToken.getEmail();
		System.out.println("u "+uid);
		
		UserRecord userRecord =auth.getUser(decodedToken.getUid());
		// See the UserRecord reference doc for the contents of userRecord.
		System.out.println("Successfully fetched user data: " + userRecord.isDisabled());
		*/


    }

    public static void createUser() throws FirebaseAuthException {
        CreateRequest request = new CreateRequest()
                .setEmail("user@example.com")
                .setEmailVerified(true)
                .setPassword("David12rtegh1iwduhiwuydgi")
                .setPhoneNumber("+11234567890")
                .setDisplayName("John Doe")
                .setPhotoUrl("http://www.example.com/12345678/photo.png")
                .setDisabled(false);

        auth.createUser(request);
    }

    public static class User {

        public String date_of_birth;
        public String full_name;
        public String nickname;


        public User(String dateOfBirth, String fullName) {
            date_of_birth = dateOfBirth;
            // ...
            full_name = fullName;
        }

        public User(String dateOfBirth, String fullName, String nickname) {
            date_of_birth = dateOfBirth;
            // ...
            full_name = fullName;
            this.nickname = nickname;
        }

    }
}
