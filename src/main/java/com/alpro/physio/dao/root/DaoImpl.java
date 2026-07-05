package com.alpro.physio.dao.root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alpro.physio.dao.UserDAO;
import com.alpro.physio.dao.CourseCatalogDAO;
import com.alpro.physio.dao.CoursesDAO;

@Component
public class DaoImpl implements Dao {
    
    @Autowired
    UserDAO userDAO;

    @Autowired
    CourseCatalogDAO courseCatalogDAO;

    @Autowired
    CoursesDAO coursesDAO;

    public UserDAO userDAO() {
        return userDAO;
    }

    public CourseCatalogDAO courseCatalogDAO() {
        return courseCatalogDAO;
    }

    public CoursesDAO coursesDAO() {
        return coursesDAO;
    }
}
