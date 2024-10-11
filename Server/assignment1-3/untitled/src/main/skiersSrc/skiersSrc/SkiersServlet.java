package skiersSrc;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SkiersServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set response content type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Parse JSON input
        ObjectMapper mapper = new ObjectMapper();
        LiftRide liftRide = null;
        try {
            liftRide = mapper.readValue(request.getInputStream(), LiftRide.class);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid JSON\"}");
            return;
        }

        // Validate parameters
        if (!isValidLiftRide(liftRide)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"message\":\"Invalid parameters\"}");
            return;
        }

        // If valid, return 201 Created
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("{\"message\":\"Lift ride recorded\"}");
    }

    private boolean isValidLiftRide(LiftRide liftRide) {
        if (liftRide == null) return false;
        if (liftRide.getSkierID() < 1 || liftRide.getSkierID() > 100000) return false;
        if (liftRide.getResortID() < 1 || liftRide.getResortID() > 10) return false;
        if (liftRide.getLiftID() < 1 || liftRide.getLiftID() > 40) return false;
        if (!"2024".equals(liftRide.getSeasonID())) return false;
        if (!"1".equals(liftRide.getDayID())) return false;
        if (liftRide.getTime() < 1 || liftRide.getTime() > 360) return false;
        return true;
    }


}

