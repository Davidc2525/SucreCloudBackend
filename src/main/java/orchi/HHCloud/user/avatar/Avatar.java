package orchi.HHCloud.user.avatar;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import orchi.HHCloud.Start;
import orchi.HHCloud.store.StoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;

public class Avatar {
    private static Logger log = LoggerFactory.getLogger(Avatar.class);
    public String id;
    public String hash;
    public Bound bound;

    public Avatar(String id, Bound bound) {
        log.info("New Avatar {} {}",id,bound);
        /*Path path = Paths.get("avatars",id+"_hash");
        if(sp().exists(path)){
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            sp().read(path,bout);
            hash = new String(bout.toByteArray());
        }else{
            try {
                long newid = System.currentTimeMillis();
                MessageDigest md = MessageDigest.getInstance("MD5");

                sp().create(path,new ByteArrayInputStream(hex( md.digest((newid+"").getBytes())  ).getBytes()));
                hash = hex(md.digest(id.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
*/
        this.id = id;
        this.bound = bound;
    }

    public Avatar(String id) {
        this(id, new Bound(0, 0, 10));
    }

    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public static StoreProvider sp() {
        return Start.getStoreManager().getStoreProvider();
    }

    public void readStream(OutputStream out) throws IOException {
        readStream(out, bound);
    }

    public void readStream(OutputStream out, Bound b) throws IOException {
        log.debug("avatar readStream {}",id);

        if (b.getHeight() == 0 || b.getWidth() == 0) {
            if (sp().exists(Paths.get("avatars", id))) {
                log.debug("PATH: " + id + " exist");

                sp().read(Paths.get("avatars", id), out);
                return;
            } else {
                log.debug("PATH: " + id + " not exist");
                defaultImage(out);
                return;
            }
        }

        if (sp().exists(Paths.get("avatars", id))) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();

            sp().read(Paths.get("avatars", id), bout);

            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(bout.toByteArray()));
            BufferedImage thumbnail = Thumbnails.of(originalImage)
                    .size(b.getWidth(), b.getHeight())
                    .crop(Positions.CENTER)
                    .asBufferedImage();
            log.debug("writing img {}",id);
            ImageIO.write(thumbnail, "png", out);
            log.debug("writed img {}",id);
        } else {
            defaultImage(out);
            //imagen por defecto si no tiene avatar
        }
    }

    private void defaultImage(OutputStream out) throws IOException {
        log.debug("Send default avatar");
        InputStream defaultAvatar = Start.class.getResourceAsStream("/da.png");

        BufferedImage originalImage = ImageIO.read(defaultAvatar);
        /*BufferedImage thumbnail = Thumbnails.of(originalImage)
                .size(500,500)
                .crop(Positions.CENTER)
                .asBufferedImage();*/

        log.debug("writing img default");
        ImageIO.write(originalImage, "png", out);
        log.debug("writed img default");
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Bound getBound() {
        return bound;
    }

    public void setBound(Bound bound) {
        this.bound = bound;
    }

}
