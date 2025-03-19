import java.awt.*; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


class Aluno implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    private double[][] notas = new double[4][3];
    private Double mediaArredondada = null; // Média final arredondada
    private Double[] mediasBimestraisArredondadas = new Double[4]; // Médias dos bimestres arredondadas

    public Aluno(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNota(int bimestre, int tipoNota, double nota) {
        if (bimestre >= 1 && bimestre <= 4 && tipoNota >= 1 && tipoNota <= 3) {
            notas[bimestre - 1][tipoNota - 1] = nota;
            mediasBimestraisArredondadas[bimestre - 1] = null; // Resetar arredondamento ao modificar notas
            mediaArredondada = null; // Resetar arredondamento final
        }
    }

    public double calcularMediaBimestre(int bimestre) {
        if (mediasBimestraisArredondadas[bimestre - 1] != null) {
            return mediasBimestraisArredondadas[bimestre - 1]; // Usa a média arredondada caso exista
        }
        double soma = 0;
        for (int i = 0; i < 3; i++) {
            soma += notas[bimestre - 1][i];
        }
        return soma / 3;
    }

    public double calcularMediaFinal() {
        if (mediaArredondada != null) {
            return mediaArredondada; // Se houver uma média final arredondada, usa ela
        }
        double soma = 0;
        for (int i = 0; i < 4; i++) {
            soma += calcularMediaBimestre(i + 1); // Usa as médias bimestrais (já considerando arredondamento)
        }
        return soma / 4;
    }

    public void arredondarMediaBimestre(int bimestre, boolean paraCima) {
        double mediaAtual = calcularMediaBimestre(bimestre);
        mediasBimestraisArredondadas[bimestre - 1] = paraCima ? Math.ceil(mediaAtual) : Math.floor(mediaAtual);
    }

    public void arredondarMediaFinal(boolean paraCima) {
        double mediaAtual = calcularMediaFinal();
        this.mediaArredondada = paraCima ? Math.ceil(mediaAtual) : Math.floor(mediaAtual);
    }

    public String getStatus() {
        double mediaFinal = calcularMediaFinal();
        if (mediaFinal >= 6) {
            return "Aprovado";
        } else if (mediaFinal >= 4) {
            return "Aprovado pela banca";
        } else {
            return "Reprovado";
        }
    }
}


class SistemaGestao implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Aluno> alunos = new ArrayList<>();

    public void adicionarAluno(String nome) {
        alunos.add(new Aluno(nome));
    }

    public void removerAluno(String nome) {
        alunos.removeIf(aluno -> aluno.getNome().equalsIgnoreCase(nome));
    }

    public Aluno buscarAluno(String nome) {
        for (Aluno aluno : alunos) {
            if (aluno.getNome().equalsIgnoreCase(nome)) {
                return aluno;
            }
        }
        return null;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }

    public void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("dados_alunos.dat"))) {
            oos.writeObject(alunos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void carregarDados() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("dados_alunos.dat"))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                alunos = (ArrayList<Aluno>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            alunos = new ArrayList<>();
        }
    }
}

public class GestorNotas extends JFrame {
    private SistemaGestao sistema = new SistemaGestao();
    private JTextField nomeAlunoField, notaField;
    private JComboBox<String> bimestreBox, tipoNotaBox;
    private JTable tabelaAlunos;
    private DefaultTableModel modeloTabela;

    public GestorNotas() {
        setTitle("Gestor de Notas");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        sistema.carregarDados();

        JPanel painelSuperior = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        nomeAlunoField = new JTextField(15);
        notaField = new JTextField(5);
        bimestreBox = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        tipoNotaBox = new JComboBox<>(new String[]{"Prova", "Teste", "Ativ. Extra"});
        JButton addAlunoBtn = new JButton("Adicionar Aluno");
        JButton removeAlunoBtn = new JButton("Remover Aluno");
        JButton addNotaBtn = new JButton("Adicionar Nota");
        JButton atualizarTabelaBtn = new JButton("Atualizar Tabela");
        JButton salvarBtn = new JButton("Salvar Dados");

        linha1.add(new JLabel("Nome:"));
        linha1.add(nomeAlunoField);
        linha1.add(addAlunoBtn);
        linha1.add(removeAlunoBtn);
        linha1.add(new JLabel("Bimestre:"));
        linha1.add(bimestreBox);

        linha2.add(new JLabel("Tipo:"));
        linha2.add(tipoNotaBox);
        linha2.add(new JLabel("Nota:"));
        linha2.add(notaField);
        linha2.add(addNotaBtn);
        linha2.add(atualizarTabelaBtn);
        linha2.add(salvarBtn);
        
        painelSuperior.add(linha1);
        painelSuperior.add(linha2);

        modeloTabela = new DefaultTableModel(new Object[]{"Aluno", "B1", "B2", "B3", "B4", "Média Final", "Status"}, 0);
        tabelaAlunos = new JTable(modeloTabela);
        JScrollPane scrollPane = new JScrollPane(tabelaAlunos);

        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

       tabelaAlunos.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        int row = tabelaAlunos.getSelectedRow();
        int col = tabelaAlunos.getSelectedColumn();

        if (row >= 0 && col >= 1 && col <= 5) { // Permitir arredondamento para bimestres e média final
            String nome = (String) modeloTabela.getValueAt(row, 0);
            Aluno aluno = sistema.buscarAluno(nome);

            if (aluno != null) {
                double valorAtual;
                boolean ehMediaFinal = (col == 5);

                if (ehMediaFinal) {
                    valorAtual = aluno.calcularMediaFinal();
                } else {
                    valorAtual = aluno.calcularMediaBimestre(col);
                }

                int escolha = JOptionPane.showOptionDialog(null,
                        "Média atual: " + valorAtual + "\nComo deseja arredondar?",
                        "Arredondamento",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Para Cima", "Para Baixo"},
                        "Para Cima");

                if (escolha != -1) { // Se o usuário escolheu uma opção
                    boolean paraCima = (escolha == 0);
                    if (ehMediaFinal) {
                        aluno.arredondarMediaFinal(paraCima);
                    } else {
                        aluno.arredondarMediaBimestre(col, paraCima);
                    }

                    // Salvar os dados e atualizar a tabela
                    sistema.salvarDados();
                    atualizarTabela();
                }
            }
        }
    }
});

        addAlunoBtn.addActionListener(e -> {
            String nome = nomeAlunoField.getText();
            sistema.adicionarAluno(nome);
            atualizarTabela();
        });

        removeAlunoBtn.addActionListener(e -> {
            String nome = nomeAlunoField.getText();
            sistema.removerAluno(nome);
            atualizarTabela();
        });

        addNotaBtn.addActionListener(e -> {
            String nome = nomeAlunoField.getText();
            Aluno aluno = sistema.buscarAluno(nome);
            if (aluno != null) {
                int bimestre = bimestreBox.getSelectedIndex() + 1;
                int tipoNota = tipoNotaBox.getSelectedIndex() + 1;
                double nota = Double.parseDouble(notaField.getText());
                aluno.setNota(bimestre, tipoNota, nota);
                atualizarTabela();
            }
        });

        atualizarTabelaBtn.addActionListener(e -> atualizarTabela());
        salvarBtn.addActionListener(e -> sistema.salvarDados());
        
    }

    private void atualizarTabela() {
    modeloTabela.setRowCount(0);
    for (Aluno aluno : sistema.getAlunos()) {
        Object[] linha = new Object[7];
        linha[0] = aluno.getNome();
        for (int i = 1; i <= 4; i++) {
            linha[i] = String.format("%.2f", aluno.calcularMediaBimestre(i)); // Exibe média bimestral (arredondada se houver)
        }
        linha[5] = String.format("%.2f", aluno.calcularMediaFinal()); // Exibe média final (arredondada se houver)
        linha[6] = aluno.getStatus();
        modeloTabela.addRow(linha);
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GestorNotas().setVisible(true));
    }
}

