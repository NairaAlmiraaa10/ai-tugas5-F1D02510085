import java.util.*;
public class SistemPakarTanaman {
    public static void main(String[] args) throws java.io.FileNotFoundException {
        Scanner scanner = new Scanner (System.in);
        //1. Baca file: blok {aturan} dan blok {sumber}
        List<Aturan> daftarAturan = new ArrayList<>();
        Map<String, String> sumberAturan = new HashMap<>();        // id aturan -> kode sumber
        Map<String, String> daftarSumber = new LinkedHashMap<>();  // kode sumber -> keterangan
        Scanner fileAturan = new Scanner(new java.io.File("AturanFile.txt"));
        String bagian = "";
        while (fileAturan.hasNextLine()) {
            String baris = fileAturan.nextLine().replace("\uFEFF", "").trim();
            if (baris.isEmpty() || baris.startsWith("#")) continue;   // baris kosong/komentar dilewati
            if (baris.startsWith("{")) {                               // awal blok
                bagian = baris.substring(1).trim().toLowerCase();
                continue;
            }
            if (baris.equals("}")) {                                   // akhir blok
                bagian = "";
                continue;
            }
            if (bagian.equals("aturan")) {
                String[] p = baris.split("\\|");                       // potong tanda |
                if (p.length < 4) continue;
                String[] syarat = p[2].split(";");                     // potong tanda ;
                for (int i = 0; i < syarat.length; i++) syarat[i] = syarat[i].trim();
                daftarAturan.add(new Aturan(p[0].trim(), syarat, p[1].trim(), p[3].trim()));
                if (p.length >= 5) sumberAturan.put(p[0].trim(), p[4].trim());
            } else if (bagian.equals("sumber")) {
                String[] s = baris.split("\\|", 2);                    // kode | keterangan
                if (s.length == 2) daftarSumber.put(s[0].trim(), s[1].trim());
            }
        }
        fileAturan.close();

        //2. Memori awal untuk fakta yang diketahui dari user
        Set<String> memori = new LinkedHashSet<>();
        Map<String, String> source = new HashMap<>();
        Set<String> sumberDipakai = new LinkedHashSet<>();            // kode sumber dari aturan yang aktif

        //3. Masukkan List Pertanyaan buat User nanti
        String [] pertanyaan = {"Daun bagian bawah menguning", "Tanaman layu padahal tanah masih basah", "Air menggenang di tatakan pot", "Tanaman terkena sinar matahari langsung yang terik",
                                "Daun tampak pucat atau memutih pada bagian tertentu","Bagian daun yang pucat berubah cokelat dan rapuh", "Pertumbuhan tanaman terhambat"};
        //4. mulai masuk tahap analisis
        System.out.println("=== ANALISIS DIAGNOSIS TINGKAT KERUSAKAN TANAMAN ===");
        System.out.println("Silahkan menjawab pertanyaan dengan jawaban ya = y, tidak = n.");

        //5. membuat loop untuk pertanyaan
        for ( String gejala: pertanyaan){
            System.out.println("Apakah " + gejala + "? (y/t)");
            String jawaban = scanner.nextLine().trim();
            if (jawaban.equalsIgnoreCase("y")){
                memori.add(gejala);
            }
        }
        System.out.print("Jawaban dari user disimpan dalam memori \n");
         //6. Algoritma FC
         boolean perubahan;
         int siklus = 1;
         do {
            perubahan = false;
            for (Aturan r:daftarAturan){
                if (memori.contains(r.maka)) continue;
                boolean syaratterpenuhi = false;
                if (r.mode.equals("dan")){
                    syaratterpenuhi = true;
                    for (String syarat : r.jika){
                        if (!memori.contains(syarat)){
                            syaratterpenuhi = false;
                            break;
                        }
                    }
                }
                else {
                    syaratterpenuhi = false;
                    for (String syarat : r.jika){
                        if (memori.contains(syarat)){
                            syaratterpenuhi = true;
                            break;
                        }
                    }
                }
                if (syaratterpenuhi){
                    memori.add(r.maka);
                    source.put(r.maka, r.id + " (" + String.join(", ", r.jika) + ")");
                    if (sumberAturan.containsKey(r.id)) sumberDipakai.add(sumberAturan.get(r.id));
                    System.out.println("Siklus "+ siklus+ ": "+ r.id + " = " + r.maka);
                    perubahan=true;
                    siklus++;
                }
            }
         }while(perubahan);

         //7. Output
         System.out.println("=== KESIMPULAN JAWABAN ===");
         String kesimpulanAkhir = null;

        for (String m : memori) {
            if (m.toLowerCase().contains("tingkat kerusakan")) {
                kesimpulanAkhir = m; 
            }
        }
        if (kesimpulanAkhir != null) {
            System.out.println("Kesimpulan: " + kesimpulanAkhir);
            System.out.println("Alasan : " + source.get(kesimpulanAkhir));

            //8. Informasi sumber
            System.out.println("=== SUMBER ===");
            for (String kode : sumberDipakai) {
                System.out.println(kode + ": " + daftarSumber.getOrDefault(kode, "(keterangan sumber tidak ditemukan)"));
            }
        } else {
            System.out.println("Tidak ada kesimpulan");
        }
        scanner.close();
    }
}