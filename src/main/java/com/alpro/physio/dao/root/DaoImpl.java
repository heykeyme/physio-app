package com.alpro.physio.dao.root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alpro.physio.dao.UserDAO;

@Component
public class DaoImpl implements Dao {
    
    @Autowired
    UserDAO userDAO;

    public UserDAO userDAO() {
        return userDAO;
    }
}
