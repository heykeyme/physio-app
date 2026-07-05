package com.alpro.physio.dao.root;

import com.alpro.physio.dao.UserDAO;
import com.alpro.physio.dao.CourseCatalogDAO;
import com.alpro.physio.dao.CoursesDAO;

public interface Dao {
    
    UserDAO userDAO();

    CourseCatalogDAO courseCatalogDAO();

    CoursesDAO coursesDAO();
}
