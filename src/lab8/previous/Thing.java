package lab8.previous;

import java.io.Serializable;
import java.util.ResourceBundle;

public class Thing implements Serializable {

    private final Item itemType;
    private ResourceBundle bundle;

    public Thing(Item itemType) {
        this.itemType = itemType;
    }

    public String content(){
        switch (itemType) {
        case TOOTHBRUSH: return bundle.getString("checkbox.toothbrush");
        case DENTIFRIECE: return bundle.getString("checkbox.dentifriece");
        case SOAP: return bundle.getString("checkbox.soap");
        case TOWEL: return bundle.getString("checkbox.towel");
        case CHIEF: return bundle.getString("checkbox.chief");
        case SOCKS: return bundle.getString("checkbox.socks");
        case NAIL: return bundle.getString("checkbox.nail");
        case COPPERWIRE: return bundle.getString("checkbox.copperwire");
        default: return "";
    }
    }
    public String rus() {
        switch (itemType) {
            case TOOTHBRUSH: return "зубная щётка";
            case DENTIFRIECE: return "зубной порошок";
            case SOAP: return "мыло";
            case TOWEL: return "полотенце";
            case CHIEF: return "носовой платок";
            case SOCKS: return "носки";
            case NAIL: return "гвоздь";
            case COPPERWIRE: return "проволока";
            default: return "";
        }
    }

    public void setBundle(ResourceBundle bundle){
        this.bundle=bundle;
    }

    public Item getItemType() {
        return itemType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Thing && ((Thing) obj).itemType == itemType;
    }
}
