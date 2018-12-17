package orchi.HHCloud.user.avatar;

public class Bound {
    public int width;
    public int height;
    public int quality;

    public Bound(){
        this.width = 0;
        this.height = 0;
        this.quality = 10;
    };

    public Bound(int width, int height, int quality) {
        this.width = width;
        this.height = height;
        this.quality = quality;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }



    public static Bound from(String sb /*String Bound*/) {
        Bound bound = new Bound();
        String ssb = sb;
        String[] parts;
        ssb = ssb.toLowerCase();

        if(ssb.contains("x")){
            parts = ssb.split("x");
            if(parts.length>0){
                try{
                    bound.setWidth(Integer.valueOf(parts[0]));
                    bound.setHeight(Integer.valueOf(parts[1]));
                }catch(NumberFormatException e){
                    e.printStackTrace();

                    bound.setWidth(500);
                    bound.setHeight(500);
                }
            }
        }

        return bound;
    }
}
