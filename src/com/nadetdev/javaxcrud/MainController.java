/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML2.java to edit this template
 */
package com.nadetdev.javaxcrud;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author NadetDev
 */
public class MainController implements Initializable {
    
    @FXML
    private Label lblID;
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblAuthor;
    @FXML
    private Label lblYear;
    @FXML
    private Label lblPages;
    @FXML
    private TextField txtId;
    @FXML
    private TextField txtTitle;
    @FXML
    private TextField txtAuthor;
    @FXML
    private TextField txtYear;
    @FXML
    private TextField txtPages;
    @FXML
    private TableView<Books> tabBookList;
    @FXML
    private TableColumn<Books, Integer> tabColID;
    @FXML
    private TableColumn<Books, String> tabColTitle;
    @FXML
    private TableColumn<Books, String> tabColAuthor;
    @FXML
    private TableColumn<Books, Integer> tabColYear;
    @FXML
    private TableColumn<Books, Integer> tabColPages;
    @FXML
    private Button btnAddNew;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnDelete;
    @FXML
    private MenuBar Menu;
    @FXML
    private MenuItem menuClose;
    @FXML
    private MenuItem menuAdd;
    @FXML
    private MenuItem menuUpdate;
    @FXML
    private MenuItem menuDelete;
    @FXML
    private Menu menuAbout1;
    @FXML
    private MenuItem menuAbout2;
    
    private void handleButtonAction(ActionEvent event){
        
        if(event.getSource() == btnAddNew){
        insertBook();
        } 
        else if (event.getSource() == btnUpdate){
        updateBook();
        } 
        else if (event.getSource() == btnDelete){
        deleteBook();
        }
        else if (event.getSource() == menuAbout2){
        ShowNadetDev();
        }
        else if (event.getSource() == menuClose){
        CloseApp();
        }
    }
    
    @FXML
    private void handleMouseAction(MouseEvent event){
    Books selectedBook = tabBookList.getSelectionModel().getSelectedItem();
    
        txtId.setText(""+selectedBook.getId());
        txtTitle.setText(""+selectedBook.getTitle());
        txtAuthor.setText(""+selectedBook.getAuthor());
        txtYear.setText(""+selectedBook.getYear());
        txtPages.setText(""+selectedBook.getPages());
    }
    
    /*
    SQL Statement
    */
    Statement pStatement;
    ResultSet rSet;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showBooks();
    }  
    //Database connection
    public Connection getConnection(){
        Connection conn;
        
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafx_books_store", "root", "P@ssword01");
           System.out.println("DB Connection Ok");
            return conn;
            
        } catch (SQLException ex) {
            System.out.println("Error: " + ex.getMessage());
            return null;
        }
    }
    
    //Get list of all books
    public ObservableList<Books> getBooksList(){
        ObservableList<Books> bookList = FXCollections.observableArrayList();
        Connection conn = getConnection();

        try {
            String qrySelectAllBooks = "SELECT * FROM books";
            pStatement = conn.createStatement();
            rSet = pStatement.executeQuery(qrySelectAllBooks);
            Books books;
            
            while (rSet.next()) {  
                books = new Books(
                rSet.getInt("id"),
                rSet.getString("title"),
                rSet.getString("author"),
                rSet.getInt("year"),
                rSet.getInt("pages")
                );
                
                bookList.add(books);
            }
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage()); 
        }
        
        return bookList;
    }
    
    //Show all books
    public void showBooks(){
        ObservableList<Books> showBooksList = getBooksList();
        
        tabColID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tabColTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tabColAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        tabColYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        tabColPages.setCellValueFactory(new PropertyValueFactory<>("pages"));
        
        tabBookList.setItems(showBooksList);
    }
    
    @FXML
    public void insertBook() {
    
        Connection conn = getConnection();
        
        try {
            String qryInsertBook = "INSERT INTO books (title, author, year, pages) VALUES (?, ?, ?, ?)";
            PreparedStatement preStatement = conn.prepareStatement(qryInsertBook);
            preStatement.setString(1, txtTitle.getText());
            preStatement.setString(2, txtAuthor.getText());
            preStatement.setString(3, txtYear.getText());
            preStatement.setString(4, txtPages.getText());
            
            preStatement.executeUpdate();
            
            //Clear components
            clearComponentText();
            
            //Reload books list
            showBooks();
            
            System.out.println("New book inserted successfully !");
            
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage()); 
        }
    }
    
    @FXML
    public void updateBook(){
        String qryUpdateBook = "UPDATE books SET title='" + txtTitle.getText() + "', author='" + txtAuthor.getText() + "'"
                + ", year='" + txtYear.getText() + "', pages='" + txtYear.getText() 
                + "' WHERE id='" + txtId.getText() + "'";
        
        excuteQuery(qryUpdateBook, "updated");
        
        //Clear components
        clearComponentText();
            
        //Reload books list
        showBooks();
    }
    
    @FXML
     public void deleteBook(){
        String qryDeleteBook = "DELETE FROM books WHERE id='" + txtId.getText() +"'";
        
        excuteQuery(qryDeleteBook, "deleted");
        
        //Clear components
        clearComponentText();
            
        //Reload books list
        showBooks();            
    }
     
    public void excuteQuery(String query, String action){
        Connection conn = getConnection();
        Statement exStatement;
        
        try {
            exStatement = conn.createStatement();
            exStatement.executeUpdate(query);
            System.out.println("New book " + action + " successfully !");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void clearComponentText(){
        txtId.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtYear.setText("");
        txtPages.setText("");
        
        //Title get focus
        txtTitle.requestFocus();
    }
    
    @FXML
    public void ShowNadetDev(){
        Dialog<String> diaglog = new Dialog<>();
        diaglog.setTitle("About");
        ButtonType type = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        diaglog.setContentText("13/10/2022"
                + "\nNadetDev JavaFx Application Tutorial."
                + "\nGluon JavaFX 19, JavaEE 8"
                + "\nZulu Java FX SDK 8"
                + "\nMariaDB Communauty edition"
                + "\nMySQL Connector J 8.0.30"
                + "\nNetBeans 15"
                + "\n + un peu de passion !");
        diaglog.getDialogPane().getButtonTypes().add(type);
        diaglog.showAndWait();
    }
    
    @FXML
    public void CloseApp(){
        Platform.exit();
    }
    
    
}
