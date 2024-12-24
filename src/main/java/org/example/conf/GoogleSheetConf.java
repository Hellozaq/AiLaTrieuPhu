package org.example.conf;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.example.model.DatabaseProperties;

import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class GoogleSheetConf {
    private static Sheets getSheets() {
        try {
            final String jsonKeyPath = "src/main/java/org/example/conf/google-json-conf.json";
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonKeyPath))
                    .createScoped(SheetsScopes.SPREADSHEETS_READONLY);

            return new Sheets.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("Read Specific Sheet Example").build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<List<Object>> getValues(final String spreadsheetId, String sheetName) {
        try {
            Sheets sheetsService = getSheets();

            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName)
                    .execute();
            return response.getValues();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            DatabaseProperties prop = new DatabaseProperties();
            prop.setDatabaseName("ailatrieuphu");
            prop.setHostname("localhost");
            prop.setPort(3306);
            prop.setPassword("");
            prop.setUsername("root");
            Database.connect(prop);
            PreparedStatement preparedStatement = Database.getConnection().prepareStatement(
                    "DELETE FROM `questions`"
            );
            preparedStatement.executeUpdate();

            final String spreadsheetId = "1YNdb2SJbfnhtPvNiJRpTfmar2V_gL5MSniGuNVE3XtU";
            final String sheetName = "question";
            List<List<Object>> values = GoogleSheetConf.getValues(spreadsheetId, sheetName);
            if (values == null || values.isEmpty()) {
                System.out.println("Không có dữ liệu trong sheet: " + sheetName);
            } else {
                StringBuilder query = new StringBuilder();
                query.append("INSERT INTO `questions` (`id`, `question`, `option_a`, `option_b`, ")
                        .append("`option_c`, `option_d`, `correct_answer`, `difficulty_level`) VALUES ");
                int idFix = 0;
                for (int i = 1; i < values.size(); i++) {
                    List<Object> row = values.get(i);
                    if (i == 1){
                        idFix =Integer.parseInt(row.getFirst().toString()) - 1;
                    }

                    String question = row.get(1).toString().replace("\"","'");
                    query
                            .append("(")
                            .append(idFix + i).append(",\"")
                            .append(question).append("\",\"")
                            .append(row.get(2)).append("\",\"")
                            .append(row.get(3)).append("\",\"")
                            .append(row.get(4)).append("\",\"")
                            .append(row.get(5)).append("\",")
                            .append(row.get(6)).append(",")
                            .append(row.get(7));
                    if (i != values.size() - 1) {
                        query.append("),");
                    } else {
                        query.append(");");
                    }
                }
                System.out.println(query.toString());
                Statement statement = Database.getConnection().createStatement();
                statement.execute(query.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
// choi thu di ad =))