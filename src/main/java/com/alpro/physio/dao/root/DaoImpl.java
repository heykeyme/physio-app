package com.alpro.physio.dao.root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alpro.physio.dao.UserDAO;
import com.alpro.physio.dao.CourseDAO;
import com.alpro.physio.dao.CourseFeedbackDAO;
import com.alpro.physio.dao.EnrollCourseDAO;
import com.alpro.physio.dao.ModuleDAO;

@Component
public class DaoImpl implements Dao {
    
    @Autowired
    UserDAO userDAO;

    @Autowired
    CourseDAO courseDAO;

    @Autowired
    EnrollCourseDAO enrollCourseDAO;

    @Autowired
    CourseFeedbackDAO courseFeedbackDAO;

    @Autowired
    ModuleDAO moduleDAO;

    public UserDAO userDAO() {
        return userDAO;
    }

    public CourseDAO courseDAO() {
        return courseDAO;
    }

    public EnrollCourseDAO enrollCourseDAO() {
        return enrollCourseDAO;
    }

    public CourseFeedbackDAO courseFeedbackDAO() {
        return courseFeedbackDAO;
    }

    public ModuleDAO moduleDAO(){
        return moduleDAO;
    }
}
