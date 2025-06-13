package projectLabo.visitors.execution;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class DictValue implements Value {
    private final TreeMap<Integer, Value> dict; // Creiamo il dizionario come Albero

    public DictValue(){ // Costruttore
        this.dict = new TreeMap<>();
    }

    public DictValue(TreeMap<Integer, Value> dict){ // Creazione di una copia
        this. dict = new TreeMap<>(dict);
    }

    public TreeMap<Integer, Value> getMap(){ // Getter dell'Albero
        return dict;
    }

    public void update(Integer key, Value value){ // Metodo put
        dict.put(key, value);
    }

    public Value get(Integer key){ // Metodo get
        return dict.get(key);
    }

    public void delete(Integer key){ // Metodo remove
        dict.remove(key);
    }

    public boolean containsKey(Integer key){ // metodo per verificare se una chiave esiste
        return dict.containsKey(key);
    }

    public DictValue copy(){ // metodo per copiare un dizionario, necessario affinche' si rispetti il comportamento funzionale
        DictValue newDict = new DictValue();
        newDict.dict.putAll(this.dict); // copio il dizionario con il metodo predefinito putAll di TreeMap
        return newDict;
    }

    @Override // Ridefinisco i metodi Equals e HashCode
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DictValue other = (DictValue) obj;
        return Objects.equals(this.dict, other.dict);
    }


    @Override
    public int hashCode() {
        return dict.hashCode();
    }

    @Override
    public String toString(){ // toString con formattazione richiesta da file di test
        // return dict.toString();
        StringBuilder str = new StringBuilder();
        str.append("[");
        boolean first = true;
        for (Map.Entry<Integer, Value> entry : dict.entrySet()) {
            if (!first) {
                str.append(",");
            }
            first = false;
            str.append(entry.getKey()).append(":").append(entry.getValue());
        }
        str.append("]");
        return str.toString();
    }
}
