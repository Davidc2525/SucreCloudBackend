package orchi.HHCloud.user.search;

import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.ScoreUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Este proveedor por defecto usa un motor de indexado y busqueda empotrado, Apache Lucene.
 * Ofrece un mejor rendimiento y mayor rapides que una implementacion equivlente basada en Mysql
 *
 * @author david
 */
public class DefaultSearchUserProvider implements SearchUserProvider {
    private static Logger log = LoggerFactory.getLogger(DefaultSearchUserProvider.class);
    private DirectoryReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private IndexWriterConfig iwc;
    private Directory dir;
    private IndexWriter writer;


    public DefaultSearchUserProvider() {
        try {
            dir = FSDirectory.open(Paths.get("./resources/UserIndex"), new LockFactory() {

                @Override
                public Lock obtainLock(Directory arg0, String arg1) throws IOException {
                    // TOD
                    return new Lock() {

                        @Override
                        public void ensureValid() throws IOException {

                        }

                        @Override
                        public void close() throws IOException {
                            // TODO Auto-generated method stub

                        }
                    };
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void prepare() {
        log.debug("PREPARE");
        try {

            StandardAnalyzer ana = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(ana);
            IndexWriter w = new IndexWriter(dir, config);


            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void init() {
        log.debug("Iniciando proveedor de busqueda de usuario");
        try {
            analyzer = new SimpleAnalyzer();
            reader = DirectoryReader.open(dir);
            searcher = new IndexSearcher(reader);
            iwc = new IndexWriterConfig(analyzer);


            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            writer = new IndexWriter(dir, iwc);

        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(1);
        }

    }

    private Document createDocument(User user) {
        log.debug("Create DocumenT to  {}", user);
        Document doc = new Document();

        DataUser u = (DataUser) user;

        doc.add(new StringField("id", u.getId(), Field.Store.YES));
        StringField field = new StringField("email", u.getEmail().toLowerCase(), Field.Store.YES);
        //field.tokenStream(analyzer,new KeywordTokenizer());
        doc.add(field);
        doc.add(new StringField("username", u.getUsername().toLowerCase(), Field.Store.YES));
        doc.add(new StringField("firstname", u.getFirstName().toLowerCase(), Field.Store.YES));
        doc.add(new StringField("lastname", u.getLastName().toLowerCase(), Field.Store.YES));

        String all = String.format("%s %s %s %s %s",
                u.getEmail().toLowerCase(),
                u.getUsername().toLowerCase(),
                u.getFirstName().toLowerCase(),
                u.getLastName().toLowerCase(),
                u.getId()
        );
        doc.add(new TextField("all", all, Field.Store.YES));

        return doc;
    }

    private void addUserToIndex(IndexWriter indexWriter, User user) {
        log.debug("Add user to index {}", user);
        try {
            indexWriter.addDocument(createDocument(user));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addUserToIndex(User user) {
        log.debug("Add user to index {}", user);

        try {
            IndexWriterConfig c = new IndexWriterConfig(analyzer);
            c.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(dir, c);

            addUserToIndex(indexWriter, user);

            indexWriter.commit();
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editUserInIndex(User oldUser, User newUser) {
        log.debug("Edit user in index \n\t{}\n\t{}", oldUser, newUser);

        try {
            IndexWriterConfig c = new IndexWriterConfig(analyzer);
            c.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(dir, c);

            indexWriter.updateDocument(new Term("id", oldUser.getId()), createDocument(newUser));

            indexWriter.commit();
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeUserToIndex(IndexWriter w, User user) {
        log.debug("Remove user to index {}", user);

        try {
            w.deleteDocuments(new Term("id", user.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void removeUserToIndex(User user) {
        log.debug("Remove user to index {}", user);

        try {
            IndexWriterConfig c = new IndexWriterConfig(analyzer);
            c.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(dir, c);

            removeUserToIndex(indexWriter, user);

            indexWriter.commit();
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addAll(Users users) {
        log.debug("Add users to index {}", users);

        try {
            IndexWriterConfig c = new IndexWriterConfig(analyzer);
            c.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(dir, c);

            users.getUsers().forEach((User u) -> {
                addUserToIndex(indexWriter, u);
            });

            indexWriter.commit();
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAll(Users users) {
        log.debug("Remove users to index {}", users);

        try {
            IndexWriterConfig c = new IndexWriterConfig(analyzer);
            c.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(dir, c);

            users.getUsers().forEach((User u) -> {
                removeUserToIndex(indexWriter, u);
            });

            indexWriter.commit();
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        log.debug("Clear index");

        try {
            IndexWriterConfig c = new IndexWriterConfig(analyzer);
            c.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(dir, c);

            indexWriter.deleteAll();

            indexWriter.commit();
            indexWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public UsersFound search(String queryString) {
        queryString = queryString.toLowerCase();
        log.debug("New query in index: {}", queryString);

        UsersFound f = new UsersFound();
        try {

            DirectoryReader read = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(read);
            Query q = new MultiFieldQueryParser(new String[]{"all","firstname", "lastname", "username", "email"}, analyzer).parse(queryString);

            TopDocs docs = null;
            docs = searcher.search(q, 150);

            ScoreDoc[] hits = docs.scoreDocs;
            log.info("{}\n{}", hits, docs.totalHits);

            //System.out.println("UsersFound " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                ScoreUser fUser = new ScoreUser();
                fUser.setId(d.get("id"));
                fUser.setUsername(d.get("username"));
                fUser.setFirstName(d.get("firstname"));
                fUser.setLastName(d.get("lastname"));
                fUser.setEmail(d.get("email"));
                fUser.setScore(hits[i].score);
                //System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("name") + "\t" + d.get("email"));
                f.add(fUser);

                fUser=null;
                d.clear();
                d=null;

            }
            hits=null;
            docs = null;
            searcher = null;
            read.close();
            read = null;
            //System.err.println("fin");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        log.debug("Found query {}", f.getAll());

        return f;
    }
}
