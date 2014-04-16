package uk.co.leopardsoftware.firsttest;

/**
 * Created by pdrage on 16/04/2014.
 */
public class DocumentType {
        int ID;
        String Name;

        public DocumentType(int ID,String SuppliedName) {
            this.ID = ID;
            this.Name = SuppliedName;
        }

        public String getName() {
            return Name;
        }
        public void setName(String SuppliedName) {
            Name = SuppliedName;
        }
        public int getID() {
            return ID;
        }
        public void setID(int iD) {
            ID = iD;
        }
    }

