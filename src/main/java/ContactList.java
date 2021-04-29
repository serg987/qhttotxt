import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactList {

    private static HashMap<String, Contact> contactList = new HashMap<>();

    public void addContact(String id, String name, String group) {
        contactList.computeIfPresent(id, (s, contact) -> contact.update(name, group));
        contactList.putIfAbsent(id, new Contact(id, name, group));
    }

    class Contact {
        public String id;
        public List<String> displayNames;
        public List<String> groups;

        Contact(String id, String name, String group) {
            displayNames = new ArrayList<>();
            groups = new ArrayList<>();
            this.id = id;
            displayNames.add(name);
            groups.add(group);
        }

        Contact update(String name, String group) {
            if (name != null &&
                    !name.isEmpty() &&
                    !id.equals(name) &&
                    !displayNames.contains(name)) displayNames.add(name);
            if (group != null && !group.isEmpty() && !groups.contains(group)) groups.add(group);
            return this;
        }

        StringBuilder getNames() {
            return Commons.populateStringWithListElems(displayNames);
        }

        StringBuilder getGroups() {
            return Commons.populateStringWithListElems(groups);
        }
    }

    public static void addContactInfoToStrBuilder(StringBuilder stringBuilder, Chat chat) {
        String uin = chat.uin;
        if (contactList == null || !contactList.containsKey(uin)) return;
        Contact contact = contactList.get(uin);
        stringBuilder.append(String.format(Configuration.contact_info_in_chat_title,
                Configuration.ownNickName, chat.nickName));
        Commons.addCRtoStringBuilder(stringBuilder);
        stringBuilder.append(String.format(Configuration.contact_info_in_chat,
                contact.id,
                (contact.getNames().toString().isEmpty()) ? contact.id : contact.getNames(),
                (contact.getGroups().toString().isEmpty()) ? "No groups found" : contact.getGroups()));
        Commons.addCRtoStringBuilder(stringBuilder);
        Commons.addCRtoStringBuilder(stringBuilder);
    }

    public static void populateChatWithName(Chat chat) {
        String uin = chat.uin;
        if (chat.nickName.equals(uin) &&
                contactList.containsKey(uin) &&
                !contactList.get(uin).displayNames.isEmpty()) {
            chat.nickName = contactList.get(uin).displayNames.get(0);
        } else if (chat.nickName.isEmpty()) chat.nickName = chat.uin;
    }

    public static HashMap<String, Contact> getContactList() {
        return contactList;
    }
}
