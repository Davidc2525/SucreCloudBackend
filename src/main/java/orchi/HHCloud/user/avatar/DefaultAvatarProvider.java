package orchi.HHCloud.user.avatar;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import orchi.HHCloud.Start;
import orchi.HHCloud.cache.Cache;
import orchi.HHCloud.cache.CacheFactory;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.avatar.Exceptions.DeleteAvatarException;
import orchi.HHCloud.user.avatar.Exceptions.GetAvatarException;
import orchi.HHCloud.user.avatar.Exceptions.SetAvatarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class DefaultAvatarProvider implements AvatarProvider {
    private static Logger log = LoggerFactory.getLogger(DefaultAvatarProvider.class);
    private StoreProvider sp = Start.getStoreManager().getStoreProvider();
    //private UserProvider up = Start.getUserManager().getUserProvider();
    private Cache<String, String> hashs;
    private Cache<String, AvatarDescriptor> ADCache;
    private MessageDigest md;

    public DefaultAvatarProvider() throws Throwable {
        log.info("INICIANDO AvatarProvider ");
        hashs = CacheFactory.createLRUCache("HASH_AVATAR");
        hashs.setMaxSize(1000);

        ADCache = CacheFactory.createLRUCache("AD_CACHE");
        ADCache.setMaxSize(1000);

        md = MessageDigest.getInstance("MD5");
    }

    private static UserProvider up() {
        return Start.getUserManager().getUserProvider();
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

    @Override
    public Avatar set(User user, InputStream image) throws SetAvatarException {
        log.debug("Set new Avatar to {}({})",user.getId(),user.getUsername());
        String hash = null;
        boolean error = false;
        try {
            up().getUserById(user.getId());

            String id = user.getId();
            Path path = Paths.get("avatars", keyAvatar(user));

            AvatarDescriptor ad = ADCache.get(keyAvatar(user));

            if (ad != null) {
                log.debug("Avatar Descriptor in caceh {}",ad);
                long newid = System.currentTimeMillis();
                hash = hex(md.digest((newid + "").getBytes()));
                ad.setHash(hash);
                ad.setHashAvatar(true);
                ad.setLastModified(newid);
                ADCache.put(keyAvatar(user), ad);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream adFile = new ObjectOutputStream(baos);
                adFile.writeObject(ad);
                sp().delete(path);
                sp().create(path, new ByteArrayInputStream(baos.toByteArray()));
            } else {
                log.debug("Avatar Descriptor ins´t caceh");
                boolean existsAdFile = sp().exists(path);
                if (existsAdFile) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    sp().read(path, baos);

                    ad = (AvatarDescriptor) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
                    log.debug("Avatar Descriptor in file {}",ad);
                } else {
                    log.debug("Avatar Descriptor ins´t file ");
                    ad = new AvatarDescriptor();
                }
                long newid = System.currentTimeMillis();
                hash = hex(md.digest((newid + "").getBytes()));
                ad.setHash(hash);
                ad.setHashAvatar(true);
                ad.setLastModified(newid);
                ad.setUid(user.getId());

                ADCache.put(keyAvatar(user), ad);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream adFile = new ObjectOutputStream(baos);
                adFile.writeObject(ad);
                log.debug("Save Avatar Descriptor {}, {}",path+"",ad);
                if (existsAdFile) {

                    sp().delete(path);
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));

                } else {
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));
                }
            }


            log.debug("Creating Avatar thumbnail");
            BufferedImage thumbnail = null;
            thumbnail = Thumbnails.of(image)
                    .size(500, 500)
                    .crop(Positions.CENTER)
                    .asBufferedImage();


            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "png", bout);
            log.debug("Creating Avatar thumbnail");
            Path pathimg = Paths.get("avatars", id);
            sp().delete(pathimg);
            log.debug("Saving Avatar thumbnail");
            sp().create(pathimg, new ByteArrayInputStream(bout.toByteArray()));
            log.debug("Saved Avatar thumbnail");

        } catch (UserException e) {
            error = true;
            e.printStackTrace();
            throw new SetAvatarException(e);
        } catch (IOException e) {
            error = true;
            e.printStackTrace();
            throw new SetAvatarException(e);
        } catch (Exception e) {
            error = true;
            e.printStackTrace();
            throw new SetAvatarException(e);
        }

        Avatar a = new Avatar(user.getId(), new Bound(0, 0, 10));
        a.setHash(hash);
        return a;

    }

    @Override
    public Avatar get(User user) throws GetAvatarException {
        return get(user, new Bound(0, 0, 10));
    }

    @Override
    public Avatar get(User user, Bound size) throws GetAvatarException {
        log.debug("Get Avatar to {}({})",user.getId(),user.getUsername());
        String hash = null;
        try {
            up().getUserById(user.getId());

            String id = user.getId();
            Path path = Paths.get("avatars", keyAvatar(user));

            AvatarDescriptor ad = ADCache.get(keyAvatar(user));
            if (ad != null) {
                log.debug("Avatar Descriptor in caceh {}",ad);
                hash = ad.getHash();
                /*boolean existsAdFile = sp().exists(path);
                if (!existsAdFile) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream adFile = new ObjectOutputStream(baos);
                    adFile.writeObject(ad);
                    sp().delete(path);
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));
                }*/
            } else {
                log.debug("Avatar Descriptor ins´t caceh {}",ad);
                boolean existsAdFile = sp().exists(path);
                if (existsAdFile) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    sp().read(path, baos);

                    ad = (AvatarDescriptor) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
                    log.debug("Avatar Descriptor in file {}",ad);
                    hash = ad.getHash();
                } else {
                    log.debug("Avatar Descriptor ins´t file {}",ad);
                    ad = new AvatarDescriptor();
                }
                long newid = System.currentTimeMillis();
                String newHash = hex(md.digest((newid + "").getBytes()));
                ad.setHash(newHash);
                ad.setLastModified(newid);
                ad.setUid(user.getId());
                if(sp().exists(Paths.get("avatars",user.getId()))){
                    ad.setHashAvatar(true);
                }

                ADCache.put(keyAvatar(user), ad);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream adFile = new ObjectOutputStream(baos);
                adFile.writeObject(ad);
                log.debug("Save Avatar Descriptor {}, {}",path+"",ad);
                if (existsAdFile) {

                    sp().delete(path);
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));

                } else {
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));
                }
            }


        } catch (UserException e) {

            e.printStackTrace();
            throw new GetAvatarException(e);
        } catch (IOException e) {

            e.printStackTrace();
            throw new GetAvatarException(e);
        } catch (Exception e) {

            e.printStackTrace();
            throw new GetAvatarException(e);
        }

        Avatar avatar = new Avatar(user.getId(), size);
        avatar.setHash(hash);
        return avatar;
    }

    @Override
    public void delete(User user) throws DeleteAvatarException {
        log.debug("Delete new Avatar to {}({})",user.getId(),user.getUsername());
        String id = user.getId();
        sp.delete(Paths.get("avatars", keyAvatar(user)));
        sp.delete(Paths.get("avatars", id));
        ADCache.remove(keyAvatar(user));

    }

    @Override
    public Boolean isSet(User user) throws Exception {
        log.debug("isSet new Avatar to {}({})",user.getId(),user.getUsername());
        Boolean hasAvatar = false;
        String hash = null;
        try {
            get(user);
            up().getUserById(user.getId());

            String id = user.getId();
            Path path = Paths.get("avatars", keyAvatar(user));

            AvatarDescriptor ad = ADCache.get(keyAvatar(user));
            if (ad != null) {
                log.debug("Avatar Descriptor in caceh {}",ad);
                hasAvatar = ad.isHashAvatar();

                boolean existsAdFile = sp().exists(path);
                if (!existsAdFile) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream adFile = new ObjectOutputStream(baos);
                    adFile.writeObject(ad);
                    sp().delete(path);
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));
                }


            } else {
                log.debug("Avatar Descriptor ins´t caceh {}",ad);
                boolean existsAdFile = sp().exists(path);
                if (existsAdFile) {
                    log.debug("Avatar Descriptor in file {}",ad);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    sp().read(path, baos);

                    ad = (AvatarDescriptor) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
                    hasAvatar = ad.isHashAvatar();
                    ADCache.put(keyAvatar(user),ad);
                } else {
                    log.debug("Avatar Descriptor ins´t file {}",ad);
                    hasAvatar = false;
                    //ad = new AvatarDescriptor();
                }
               /* long newid = System.currentTimeMillis();
                String newHash = hex(md.digest((newid + "").getBytes()));
                ad.setHash(newHash);
                ad.setLastModified(newid);
                ad.setUid(user.getId());


                ADCache.put(keyAvatar(user), ad);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream adFile = new ObjectOutputStream(baos);
                adFile.writeObject(ad);
                if (existsAdFile) {

                    sp().delete(path);
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));

                } else {
                    sp().create(path, new ByteArrayInputStream(baos.toByteArray()));
                }*/
            }


        } catch (UserException e) {

            e.printStackTrace();
            throw new Exception(e);
        } catch (IOException e) {

            e.printStackTrace();
            throw new Exception(e);
        } catch (Exception e) {

            e.printStackTrace();
            throw new Exception(e);
        } catch (GetAvatarException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return hasAvatar;
    }

    @Override
    public AvatarDescriptor getADescriptor(User user) throws Exception {
        AvatarDescriptor ad = null;
        try {

            up().getUserById(user.getId());

            String id = user.getId();
            Path path = Paths.get("avatars", keyAvatar(user));

            ad = ADCache.get(keyAvatar(user));
            if (ad != null) {
                log.debug("Avatar Descriptor in caceh {}",ad);


            } else {
                log.debug("Avatar Descriptor ins´t caceh {}",ad);
                boolean existsAdFile = sp().exists(path);
                if (existsAdFile) {
                    log.debug("Avatar Descriptor in file {}",ad);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    sp().read(path, baos);

                    ad = (AvatarDescriptor) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();

                    ADCache.put(keyAvatar(user),ad);
                } else {
                    log.debug("Avatar Descriptor ins´t file {}",ad);

                    //ad = new AvatarDescriptor();
                }

            }


        } catch (UserException e) {

            e.printStackTrace();
            throw new Exception(e);
        } catch (IOException e) {

            e.printStackTrace();
            throw new Exception(e);
        } catch (Exception e) {

            e.printStackTrace();
            throw new Exception(e);
        }
        return ad;
    }


    private String keyAvatar(User user) {
        return String.format("_%s_avatar_descriptor_", user.getId());
    }
}
