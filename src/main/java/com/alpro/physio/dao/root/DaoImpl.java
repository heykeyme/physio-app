package com.alpro.physio.dao.root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alpro.physio.dao.UserDAO;
import com.alpro.physio.dao.VideoDAO;
import com.alpro.physio.dao.AssessmentDAO;
import com.alpro.physio.dao.CourseDAO;
import com.alpro.physio.dao.CourseFeedbackDAO;
import com.alpro.physio.dao.EnrollCourseDAO;
import com.alpro.physio.dao.MasterRoleDAO;
import com.alpro.physio.dao.ModuleDAO;
import com.alpro.physio.dao.PaymentTransactionDAO;
import com.alpro.physio.dao.QuestionDAO;
import com.alpro.physio.dao.QuestionOptionDAO;
import com.alpro.physio.dao.ReportsDAO;
import com.alpro.physio.dao.UploadPdfDAO;

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

    @Autowired
    MasterRoleDAO masterRoleDAO;

    @Autowired
    VideoDAO videoDAO;

    @Autowired
    UploadPdfDAO uploadPdfDAO;

    @Autowired
    AssessmentDAO assessmentDAO;

    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    QuestionOptionDAO questionOptionDAO;

    @Autowired
    PaymentTransactionDAO paymentTransactionDAO;

    @Autowired
    ReportsDAO reportsDAO;

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

    public MasterRoleDAO masterRoleDAO(){
        return masterRoleDAO;
    }

    public VideoDAO videoDAO(){
        return videoDAO;
    }

    public UploadPdfDAO uploadPdfDAO(){
        return uploadPdfDAO;
    }

    public AssessmentDAO assessmentDAO(){
        return assessmentDAO;
    }

    public QuestionDAO questionDAO(){
        return questionDAO;
    }

    public QuestionOptionDAO questionOptionDAO(){
        return questionOptionDAO;
    }

    public PaymentTransactionDAO paymentTransactionDAO(){
        return paymentTransactionDAO;
    }

    public ReportsDAO reportsDAO(){
        return reportsDAO;
    }
}
