package com.alpro.physio.dao.root;

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

public interface Dao {
    
    UserDAO userDAO();

    CourseDAO courseDAO();

    EnrollCourseDAO enrollCourseDAO();

    CourseFeedbackDAO courseFeedbackDAO();

    ModuleDAO moduleDAO();

    MasterRoleDAO masterRoleDAO();

    VideoDAO videoDAO();

    UploadPdfDAO uploadPdfDAO();

    AssessmentDAO assessmentDAO();

    QuestionDAO questionDAO();

    QuestionOptionDAO questionOptionDAO();

    PaymentTransactionDAO paymentTransactionDAO();

    ReportsDAO reportsDAO(); 
}
