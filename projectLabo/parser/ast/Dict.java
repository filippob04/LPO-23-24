package projectLabo.parser.ast;

import static java.util.Objects.requireNonNull;
import projectLabo.visitors.Visitor;

public class Dict implements Exp {

    public enum DictOp { GET, UPDATE, DELETE, CREATE } // Enum per Operazioni

    private final DictOp op;
    private final Exp dict; // solo per operazioni, null per la creazione
    private final Exp key;
    private final Exp value; // null per get e delete

    // Costruttore generale per operazioni su dizionario pre-esistente (get, update, delete)
    public Dict(DictOp op, Exp dict, Exp key, Exp value){
        this.op = requireNonNull(op);
        if(op == DictOp.CREATE){ // gestisco esplicitamente il caso della create (ad esempio se facessi una create ma con un dizionario preesistente)
            if (dict != null) {
                throw new IllegalArgumentException("CREATE operation must have null dict");
            }
            this.dict = null;
        } else{
            this.dict = requireNonNull(dict);
        }
        this.key = requireNonNull(key);
        this.value = value; // null per get e delete
    }

    // Costruttore per operazioni di get e delete
    public Dict(DictOp op, Exp dict, Exp key){
        this(op, dict, key, null);
    }

    // Costruttore per la creazione di un dizionario [key:value]
    public Dict(Exp key, Exp value){
        this.op = DictOp.CREATE;
        this.dict = null;
        this.key = requireNonNull(key);
        this.value = requireNonNull(value);
    }

    @Override
    public String toString() { // toString
        return switch (op) {
            case CREATE -> String.format("[%s:%s]", key, value);
            case GET -> String.format("(%s)[%s]", dict, key);
            case DELETE -> String.format("(%s)[%s:]", dict, key);
            case UPDATE -> String.format("(%s)[%s:%s]", dict, key, value);
        };
    }

    @Override
    public <T> T accept(Visitor<T> visitor) { // Visitor
        return visitor.visitDict(op, dict, key, value);
    }


    // Metodi Getter
    public DictOp getOp() {
        return op;
    }

    public Exp getDict() {
        return dict;
    }

    public Exp getKey() {
        return key;
    }

    public Exp getValue() {
        return value;
    }
    
}