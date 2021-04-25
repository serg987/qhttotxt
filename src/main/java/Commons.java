import java.util.List;

public class Commons {

    static void addCRtoStringBuilder(StringBuilder stringBuilder) {
        stringBuilder.append(System.getProperty("line.separator"));
    }

    static StringBuilder populateStringWithListElems(List<String> stringList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (stringList.size() == 0) return stringBuilder;
        stringBuilder.append("'");
        for (int i = 0; i < stringList.size(); i++) {
            stringBuilder.append(stringList.get(i));
            if (i != stringList.size() - 1) stringBuilder.append("', '");
        }
        stringBuilder.append("'");
        return stringBuilder;
    }
}
