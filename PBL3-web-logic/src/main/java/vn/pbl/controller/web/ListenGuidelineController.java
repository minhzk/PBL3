package vn.pbl.controller.web;

import org.apache.commons.lang.StringUtils;
import vn.pbl.command.CommentCommand;
import vn.pbl.command.ListenGuidelineCommand;
import vn.pbl.core.common.util.SessionUtil;
import vn.pbl.core.dao.CommentDao;
import vn.pbl.core.dto.CommentDTO;
import vn.pbl.core.dto.ListenGuidelineDTO;
import vn.pbl.core.service.utils.SingletonDaoUtil;
import vn.pbl.core.web.common.WebConstant;
import vn.pbl.core.web.utils.FormUtil;
import vn.pbl.core.web.utils.RequestUtil;
import vn.pbl.core.web.utils.SingletonServiceUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/danh-sach-huong-dan-nghe.html","/noi-dung-bai-huong-dan-nghe.html"})
public class ListenGuidelineController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ListenGuidelineCommand command = FormUtil.populate(ListenGuidelineCommand.class, request);
        if (request.getParameter("listenguidelineid") != null) {
            String listenGuidelineStr = request.getParameter("listenguidelineid");
            ListenGuidelineDTO existListenGuideline = SingletonServiceUtil.getListenGuidelineServiceInstance().findByListenGuidelineId("listenGuidelineId", Integer.parseInt(listenGuidelineStr));
            command.setPojo(existListenGuideline);
            Object[] obj= SingletonServiceUtil.getCommentServiceInstance().findAllCommentByProperties(null,null,null,null,null,existListenGuideline.getListenGuidelineId());
            List<CommentDTO> comment = (List<CommentDTO>)obj[1];
            request.setAttribute(WebConstant.LIST_COMMENT,comment);
            request.setAttribute(WebConstant.FORM_ITEM, command);
            RequestDispatcher rd = request.getRequestDispatcher("/views/web/listenguideline/detail.jsp");
            rd.forward(request, response);
        } else {
            executeSearchListenGuideline(request, command);
            request.setAttribute(WebConstant.LIST_ITEMS, command);
            RequestDispatcher rd = request.getRequestDispatcher("/views/web/listenguideline/list.jsp");
            rd.forward(request, response);
        }
    }

    private void executeSearchListenGuideline(HttpServletRequest request, ListenGuidelineCommand command) {
        Map<String, Object> properties = buildMapProperties(command);
        command.setMaxPageItems(3);
        RequestUtil.initSearchBeanManual(command);
        Object[] objects = SingletonServiceUtil.getListenGuidelineServiceInstance().findListenGuidelineByProperties(properties, command.getSortExpression(), command.getSortDirection(), command.getFirstItem(), command.getMaxPageItems());
        command.setListResult((List<ListenGuidelineDTO>) objects[1]);
        command.setTotalItems(Integer.parseInt(objects[0].toString()));
        command.setTotalPages((int) Math.ceil((double) command.getTotalItems() / command.getMaxPageItems()));
    }

    private Map<String,Object> buildMapProperties(ListenGuidelineCommand command) {
        Map<String, Object> properties = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(command.getPojo().getTitle())) {
            properties.put("title", command.getPojo().getTitle());
        }
        return properties;
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String listenGuidelineId = request.getParameter("listenGuidelineId");
        String email = (String) SessionUtil.getInstance().getValue(request, WebConstant.LOGIN_NAME);
        String content = request.getParameter("content");
        CommentDTO commentDTO = SingletonServiceUtil.getCommentServiceInstance().saveComment(email,Integer.parseInt(listenGuidelineId),content);
        RequestDispatcher rd = request.getRequestDispatcher("/views/web/listenguideline/detail.jsp");
        rd.forward(request, response);
    }
}
