import files.Payload;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;

public class ComplexJsonParse {

    public static void main(String[] args) {

        JsonPath jsonPath = new JsonPath(Payload.CoursePrice());

        //1. Print No of courses returned by API
        int count = jsonPath.getInt("courses.size()");
        System.out.println("Number of courses: " + count);

        //2. Print Purchase Amount
        int purchaseAmount = jsonPath.getInt("dashboard.purchaseAmount");
        System.out.println("Purchase Amount: " + purchaseAmount);

        //3. Print Title of the first course
        String firstCourseTitle = jsonPath.getString("courses[0].title");
        System.out.println("Title of the first course: " + firstCourseTitle);

        //4. Print all course titles and their respective prices
        System.out.println("Course Titles and Prices:");
        for (int i = 0; i < count; i++) {
            String title = jsonPath.getString("courses[" + i + "].title");
            int price = jsonPath.getInt("courses[" + i + "].price");
            System.out.println(title + ": " + price);
        }

        //5. Print no of copies sold by RPA Course
        for (int i = 0; i < count; i++) {
            String title = jsonPath.getString("courses[" + i + "].title");
            if (title.equalsIgnoreCase("RPA")) {
                int copies = jsonPath.getInt("courses[" + i + "].copies");
                System.out.println("Number of copies sold by RPA Course: " + copies);
                break;
            }
        }
        //6. Verify if Sum of all Course prices matches with Purchase Amount
        int totalAmount = 0;
        for (int i = 0; i < count; i++) {
            int price = jsonPath.getInt("courses[" + i + "].price");
            int copies = jsonPath.getInt("courses[" + i + "].copies");
            totalAmount += price * copies;
        }
        System.out.println("Total Amount from all courses: " + totalAmount);
        Assert.assertEquals(totalAmount, purchaseAmount);
        if (totalAmount == purchaseAmount) {
            System.out.println("Sum of all course prices matches with Purchase Amount.");
        } else {
            System.out.println("Sum of all course prices does not match with Purchase Amount.");
        }
    }
}
