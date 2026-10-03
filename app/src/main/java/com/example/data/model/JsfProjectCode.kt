package com.example.data.model

data class JsfFileItem(
    val filename: String,
    val language: String,
    val description: String,
    val content: String
)

object JsfProjectCode {
    val files: List<JsfFileItem> = listOf(
        JsfFileItem(
            filename = "pom.xml",
            language = "xml",
            description = "Configuration Maven (Jakarta EE 10, Mojarra JSF, Hibernate, MySQL Connector, PrimeFaces)",
            content = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.boutique</groupId>
    <artifactId>ecommerce-jsf-mysql</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <jakartaee.version>10.0.0</jakartaee.version>
    </properties>

    <dependencies>
        <!-- Jakarta EE Web API (JSF, CDI, Bean Validation) -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-web-api</artifactId>
            <version>${'$'}{jakartaee.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- Implementation JSF (Mojarra) -->
        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>jakarta.faces</artifactId>
            <version>4.0.4</version>
        </dependency>

        <!-- Driver MySQL JDBC -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>

        <!-- Hibernate ORM & JPA -->
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.4.4.Final</version>
        </dependency>

        <!-- PrimeFaces (Composants UI riches JSF) -->
        <dependency>
            <groupId>org.primefaces</groupId>
            <artifactId>primefaces</artifactId>
            <version>13.0.4</version>
            <classifier>jakarta</classifier>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>"""
        ),
        JsfFileItem(
            filename = "Product.java",
            language = "java",
            description = "Entité JPA mappée sur la table MySQL 'produits'",
            content = """package com.boutique.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produits")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "promo_price", precision = 10, scale = 2)
    private BigDecimal promoPrice;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "category_name", length = 150)
    private String categoryName;

    @Column(nullable = false, unique = true, length = 60)
    private String sku;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructeurs
    public Product() {}

    public Product(String name, BigDecimal price, Integer stock, String categoryName, String sku) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryName = categoryName;
        this.sku = sku;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getPromoPrice() { return promoPrice; }
    public void setPromoPrice(BigDecimal promoPrice) { this.promoPrice = promoPrice; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}"""
        ),
        JsfFileItem(
            filename = "ProductBean.java",
            language = "java",
            description = "Managed Bean JSF (@Named @ViewScoped) pour la gestion du catalogue",
            content = """package com.boutique.beans;

import com.boutique.dao.ProductDAO;
import com.boutique.model.Product;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("productBean")
@ViewScoped
public class ProductBean implements Serializable {

    @Inject
    private ProductDAO productDAO;

    private List<Product> products;
    private Product selectedProduct;
    private Product newProduct;
    private String searchKeyword;

    @PostConstruct
    public void init() {
        refreshProducts();
        newProduct = new Product();
    }

    public void refreshProducts() {
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            products = productDAO.search(searchKeyword.trim());
        } else {
            products = productDAO.findAll();
        }
    }

    public void saveProduct() {
        try {
            if (newProduct.getId() == null) {
                productDAO.create(newProduct);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Produit créé avec succès dans MySQL !");
            } else {
                productDAO.update(newProduct);
                addMessage(FacesMessage.SEVERITY_INFO, "Succès", "Produit mis à jour avec succès !");
            }
            newProduct = new Product();
            refreshProducts();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible d'enregistrer : " + e.getMessage());
        }
    }

    public void deleteProduct(Product product) {
        try {
            productDAO.delete(product.getId());
            refreshProducts();
            addMessage(FacesMessage.SEVERITY_INFO, "Suppression", "Produit supprimé de la base.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Échec de suppression : " + e.getMessage());
        }
    }

    public void prepareEdit(Product product) {
        this.newProduct = product;
    }

    public void prepareNew() {
        this.newProduct = new Product();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters & Setters
    public List<Product> getProducts() { return products; }
    public Product getSelectedProduct() { return selectedProduct; }
    public void setSelectedProduct(Product selectedProduct) { this.selectedProduct = selectedProduct; }
    public Product getNewProduct() { return newProduct; }
    public void setNewProduct(Product newProduct) { this.newProduct = newProduct; }
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }
}"""
        ),
        JsfFileItem(
            filename = "ProductDAO.java",
            language = "java",
            description = "Couche d'accès aux données (DAO) JPA / JDBC pour MySQL",
            content = """package com.boutique.dao;

import com.boutique.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductDAO {

    @PersistenceContext(unitName = "BoutiqueMySQL_PU")
    private EntityManager em;

    public List<Product> findAll() {
        return em.createQuery("SELECT p FROM Product p ORDER BY p.id DESC", Product.class)
                 .getResultList();
    }

    public Product findById(Long id) {
        return em.find(Product.class, id);
    }

    public List<Product> search(String query) {
        return em.createQuery(
            "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:q) OR LOWER(p.sku) LIKE LOWER(:q)",
            Product.class
        )
        .setParameter("q", "%" + query + "%")
        .getResultList();
    }

    @Transactional
    public void create(Product product) {
        em.persist(product);
    }

    @Transactional
    public Product update(Product product) {
        return em.merge(product);
    }

    @Transactional
    public void delete(Long id) {
        Product p = findById(id);
        if (p != null) {
            em.remove(p);
        }
    }
}"""
        ),
        JsfFileItem(
            filename = "persistence.xml",
            language = "xml",
            description = "Configuration JPA & Connexion MySQL (src/main/resources/META-INF/persistence.xml)",
            content = """<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence 
             https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd"
             version="3.0">

    <persistence-unit name="BoutiqueMySQL_PU" transaction-type="JTA">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <class>com.boutique.model.Product</class>
        <class>com.boutique.model.Category</class>
        <class>com.boutique.model.Customer</class>
        <class>com.boutique.model.Order</class>

        <properties>
            <!-- Configuration JDBC directe ou via DataSource JNDI -->
            <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver" />
            <property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/boutique_ecommerce?useSSL=false&amp;serverTimezone=UTC" />
            <property name="jakarta.persistence.jdbc.user" value="root" />
            <property name="jakarta.persistence.jdbc.password" value="root123" />

            <!-- Dialecte & Options Hibernate -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect" />
            <property name="hibernate.show_sql" value="true" />
            <property name="hibernate.format_sql" value="true" />
            <property name="hibernate.hbm2ddl.auto" value="update" />
        </properties>
    </persistence-unit>
</persistence>"""
        ),
        JsfFileItem(
            filename = "products.xhtml",
            language = "xml",
            description = "Vue Facelets JSF / PrimeFaces pour gérer les produits (src/main/webapp/products.xhtml)",
            content = """<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:p="http://primefaces.org/ui">

<h:head>
    <title>Administration Boutique - Catalogue &amp; MySQL</title>
    <meta charset="UTF-8"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"/>
</h:head>

<h:body style="background-color: #f8fafc; padding: 20px;">
    <div class="container-fluid">
        <!-- En-tête -->
        <div class="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
            <div>
                <h1 class="h3 fw-bold text-primary">Gestion de la Boutique en Ligne</h1>
                <p class="text-muted">Console d'administration JavaServer Faces &amp; Base de données MySQL</p>
            </div>
            <p:commandButton value="Nouveau Produit" icon="pi pi-plus" 
                             actionListener="#{productBean.prepareNew}"
                             oncomplete="PF('productDialog').show()" 
                             update=":dialogForm:productDialogPanel"
                             styleClass="ui-button-success" />
        </div>

        <h:form id="mainForm">
            <p:growl id="messages" showDetail="true" />

            <!-- Barre de Recherche -->
            <div class="row mb-3">
                <div class="col-md-6 d-flex gap-2">
                    <p:inputText value="#{productBean.searchKeyword}" placeholder="Rechercher nom ou SKU..." styleClass="form-control" />
                    <p:commandButton value="Filtrer" icon="pi pi-search" action="#{productBean.refreshProducts}" update="productTable messages" />
                    <p:commandButton value="Reset" icon="pi pi-refresh" action="#{productBean.init}" update="productTable @form" styleClass="ui-button-secondary" />
                </div>
            </div>

            <!-- Table des Produits -->
            <p:dataTable id="productTable" var="prod" value="#{productBean.products}" 
                         paginator="true" rows="10" responsiveLayout="scroll"
                         styleClass="shadow-sm rounded bg-white">
                
                <p:column headerText="ID" style="width: 60px; text-align: center;">
                    <h:outputText value="#{prod.id}" />
                </p:column>

                <p:column headerText="SKU" style="width: 120px;">
                    <span class="badge bg-secondary">#{prod.sku}</span>
                </p:column>

                <p:column headerText="Nom du Produit">
                    <strong>#{prod.name}</strong>
                    <div class="small text-muted">#{prod.description}</div>
                </p:column>

                <p:column headerText="Catégorie" style="width: 180px;">
                    <span class="badge bg-info text-dark">#{prod.categoryName}</span>
                </p:column>

                <p:column headerText="Prix (€)" style="width: 120px; text-align: right;">
                    <span class="fw-bold text-success">#{prod.price} €</span>
                </p:column>

                <p:column headerText="Stock" style="width: 100px; text-align: center;">
                    <h:outputText value="#{prod.stock}" 
                                  styleClass="#{prod.stock lt 5 ? 'badge bg-danger' : 'badge bg-success'}" />
                </p:column>

                <p:column headerText="Statut" style="width: 100px; text-align: center;">
                    <span class="#{prod.active ? 'badge bg-primary' : 'badge bg-secondary'}">
                        #{prod.active ? 'Actif' : 'Inactif'}
                    </span>
                </p:column>

                <p:column headerText="Actions" style="width: 150px; text-align: center;">
                    <p:commandButton icon="pi pi-pencil" 
                                     actionListener="#{productBean.prepareEdit(prod)}"
                                     oncomplete="PF('productDialog').show()" 
                                     update=":dialogForm:productDialogPanel"
                                     styleClass="ui-button-warning me-1" />

                    <p:commandButton icon="pi pi-trash" 
                                     action="#{productBean.deleteProduct(prod)}"
                                     update="productTable :mainForm:messages"
                                     styleClass="ui-button-danger">
                        <p:confirm header="Confirmation" message="Supprimer ce produit de MySQL ?" icon="pi pi-exclamation-triangle"/>
                    </p:commandButton>
                </p:column>
            </p:dataTable>
        </h:form>

        <!-- Dialogue d'Ajout / Édition -->
        <p:dialog header="Détails du Produit" widgetVar="productDialog" modal="true" width="550px" responsive="true">
            <h:form id="dialogForm">
                <p:outputPanel id="productDialogPanel" styleClass="p-fluid">
                    <div class="mb-3">
                        <label class="form-label">Nom du Produit *</label>
                        <p:inputText value="#{productBean.newProduct.name}" required="true" />
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Code SKU *</label>
                        <p:inputText value="#{productBean.newProduct.sku}" required="true" />
                    </div>
                    <div class="row">
                        <div class="col-6 mb-3">
                            <label class="form-label">Prix (€) *</label>
                            <p:inputNumber value="#{productBean.newProduct.price}" symbol=" €" symbolPosition="s" decimalSeparator="." required="true" />
                        </div>
                        <div class="col-6 mb-3">
                            <label class="form-label">Stock *</label>
                            <p:spinner value="#{productBean.newProduct.stock}" min="0" />
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Catégorie</label>
                        <p:inputText value="#{productBean.newProduct.categoryName}" />
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Description</label>
                        <p:inputTextarea value="#{productBean.newProduct.description}" rows="3" />
                    </div>
                </p:outputPanel>

                <f:facet name="footer">
                    <p:commandButton value="Enregistrer dans MySQL" icon="pi pi-check" 
                                     actionListener="#{productBean.saveProduct}"
                                     update=":mainForm:productTable :mainForm:messages"
                                     oncomplete="if (!args.validationFailed) PF('productDialog').hide()"
                                     styleClass="ui-button-primary" />
                    <p:commandButton value="Annuler" icon="pi pi-times" 
                                     onclick="PF('productDialog').hide()" 
                                     type="button" styleClass="ui-button-secondary" />
                </f:facet>
            </h:form>
        </p:dialog>
    </div>
</h:body>
</html>"""
        ),
        JsfFileItem(
            filename = "web.xml",
            language = "xml",
            description = "Descripteur de déploiement Servlet / JSF (src/main/webapp/WEB-INF/web.xml)",
            content = """<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <display-name>Boutique E-Commerce JSF</display-name>

    <welcome-file-list>
        <welcome-file>products.xhtml</welcome-file>
    </welcome-file-list>

    <!-- Faces Servlet -->
    <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>

    <!-- Thème PrimeFaces -->
    <context-param>
        <param-name>primefaces.THEME</param-name>
        <param-value>saga</param-value>
    </context-param>

    <context-param>
        <param-name>jakarta.faces.PROJECT_STAGE</param-name>
        <param-value>Development</param-value>
    </context-param>
</web-app>"""
        )
    )
}
