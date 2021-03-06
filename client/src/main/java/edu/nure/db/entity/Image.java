package edu.nure.db.entity;

import edu.nure.db.Connector;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.constraints.ValidationException;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bod on 17.09.15.
 */
public class Image implements Transmittable{
    public static final int ID_NOT_SET = -1;
    private int id = ID_NOT_SET;
    private String hash;
    private byte[] image;
    private int album;
    private Date createdIn;

    public Image(String hash, int id, byte[] image, int album, Date createdIn) {
        setHash(hash);
        setId(id);
        setImage(image);
        setAlbum(album);
        setCreatedIn(createdIn);
    }

    public Image(ResultSet rs) throws SQLException, ValidationException {
        setHash(rs.getString("Hash"));
        setId(rs.getInt("Id"));
        setImage(rs.getBytes("Image"));
        setAlbum(rs.getInt("Album"));
        setCreatedIn(rs.getDate("CreatedIn"));

    }

    public String getCreatedIn() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(createdIn);
    }

    public void setCreatedIn(Date createdIn) {
        this.createdIn = createdIn;
    }

    public int getAlbum() {
        return album;
    }

    public void setAlbum(int album) {
        this.album = album;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toXML() {
        return "<image id=\""+id+"\" hash=\""+hash+"\" album=\""
                + album +"\" createdIn=\""+getCreatedIn()+"\"/>";
    }

    @Override
    public String toQuery() {
        return "id=" + id +
                "&hash=" + hash +
                "&albumId=" + album +
                "&createdIn=" + getCreatedIn();
    }

    public static Image getImageById(int id)throws ConnectException, SQLException, ValidationException{
        ResultSet rs = Connector.getConnector().getConnection().createStatement().
                executeQuery(RequestPreparing.select("image", new String[]{"*"}, "WHERE Id = " + id));
        return new Image(rs);
    }

    public static String[] getFields() {
        return new String[]{"Hash", "Album", "CreatedIn", "Image"};
    }

    public static void main(String[] arg) throws ParseException {
    }
}
