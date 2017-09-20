package dto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by gali on 9/16/17.
 */
public class UserDto {

    public String name;
    public String lastname;
    public String firstname;

    public UserDto() {
    }

    public UserDto(ResultSet rs) throws SQLException {

        this.name = rs.getString("name_");
        this.lastname = rs.getString("lastname_");
        this.firstname = rs.getString("firstname_");

    }

}

