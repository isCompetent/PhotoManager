package edu.nure.performers;

import edu.nure.db.Connector;
import edu.nure.db.MyDataSource;
import edu.nure.db.RequestPreparing;
import edu.nure.db.entity.Album;
import edu.nure.performers.exceptions.PerformException;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Created by bod on 07.10.15.
 */
public class AlbumPerformer extends AbstractPerformer {

    public AlbumPerformer(ResponseBuilder builder) throws SQLException {
        connection = Connector.getConnector().getConnection();
        this.builder = builder;
    }

    @Override
    public void perform() throws PerformException, IOException, SQLException {
        int action = builder.getAction();
        switch (action) {
            case Action.GET_ALBUM:
                doGet();
                break;
            case Action.INSERT_ALBUM:
                doInsert();
                break;
            case Action.DELETE_ALBUM:
                doDelete();
        }
    }

    @Override
    protected void doGet() throws PerformException, IOException, SQLException {
        try{
            int id = builder.getIntParameter("id");
            ResultSet s;
            if(builder.getParameter("albumId") != null){
                // returns Album with Id = albumId
                s = getStatement().executeQuery(RequestPreparing.select("album", new String[]{"*"},
                        "WHERE Id ="+builder.getParameter("albumId")+" Limit 1;"));
            }else {
                // returns list of user's albums
                s = getStatement().executeQuery(RequestPreparing.select("album", new String[]{"*"},
                        "WHERE UserId = " + id + " Order by Name;"));
            }
            while (s.next()){
                builder.add(new Album(s));
            }
            builder.setStatus(ResponseBuilder.STATUS_OK);

        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (SQLException ex){
            throw new PerformException("Ошибка обработки запроса");
        }

    }

    @Override
    protected void doInsert() throws PerformException, IOException, SQLException {
        try{
            Album a = new Album(builder);
            getConnection().setAutoCommit(false);
            int n =  getStatement().executeUpdate(RequestPreparing.insert("album", Album.getFields(),
                    new Object[]{a.getName(), a.getUserId()}));

            if(n > 0) {
                Album album = new Album(getLastInserted("album"));
                builder.add(album);
                builder.setStatus(ResponseBuilder.STATUS_OK);
                getConnection().commit();
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка создания нового альбома");
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (SQLException ex){
            if(ex.getMessage().toLowerCase().contains("duplicate"))
                throw new PerformException("Такой альбом уже существует");
            throw new PerformException("Ошибка обработки запроса");
        } finally {
            getConnection().setAutoCommit(true);
        }
    }

    @Override
    protected void doDelete() throws PerformException, IOException, SQLException {
        try{
            Album a = new Album(builder);
            int n =  getStatement().executeUpdate(
                    "DELETE FROM `ALBUM` WHERE Id=" + a.getId()
            );

            if(n > 0) {
                builder.setStatus(ResponseBuilder.STATUS_OK);
                return;
            }
            builder.setStatus(ResponseBuilder.STATUS_ERROR_WRITE);
            builder.setText("Ошибка удаления альбома");
        }catch (NumberFormatException ex){
            throw new PerformException("Недостаточно параметров");
        } catch (SQLException ex){
            throw new PerformException("Ошибка обработки запроса: альбом содержит фотографии");
        }
    }

    public static void main(String[] args)throws SQLException{
        Connection c = new MyDataSource(1).getConnection();
        c.setAutoCommit(false);
        c.createStatement().executeUpdate("Insert INTO `Test` VALUE (10)");
        ResultSet rs = c.createStatement().executeQuery("Select MAX(`Id`) as `Id` FROM `Test`");
        while (rs.next())
            System.out.println(rs.getString(1));
        c.commit();
        c.setAutoCommit(true);
    }
}
