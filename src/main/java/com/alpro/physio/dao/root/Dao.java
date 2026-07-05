package com.alpro.physio.dao.root;

import com.alpro.physio.dao.UserDAO;
import com.alpro.physio.dao.CourseDAO;
import com.alpro.physio.dao.EnrollCourseDAO;

public interface Dao {
    
    UserDAO userDAO();

    CourseDAO courseDAO();

    EnrollCourseDAO enrollCourseDAO();
}
