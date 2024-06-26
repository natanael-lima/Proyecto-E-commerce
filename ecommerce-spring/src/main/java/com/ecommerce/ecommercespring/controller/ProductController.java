package com.ecommerce.ecommercespring.controller;



import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ecommerce.ecommercespring.dto.ProductDTO;
import com.ecommerce.ecommercespring.dto.ProductRegistrationDTO;
import com.ecommerce.ecommercespring.entity.Product;
import com.ecommerce.ecommercespring.entity.SearchHistory;
import com.ecommerce.ecommercespring.enums.ActionType;
import com.ecommerce.ecommercespring.enums.TableType;
import com.ecommerce.ecommercespring.exception.CategoryNotFoundException;
import com.ecommerce.ecommercespring.response.ApiResponse;
import com.ecommerce.ecommercespring.service.HistoryService;
import com.ecommerce.ecommercespring.service.ProductService;


@CrossOrigin(origins = "http://localhost:4200") // Reemplaza esto con el dominio de tu frontend
@RestController
@RequestMapping("/api/product")
public class ProductController {
	
	@Autowired
    private ProductService productService;
	@Autowired
    private HistoryService historyService;

	// API para obtener un producto by ID.
    @GetMapping("/get-product/{id}")
    public ResponseEntity<ProductDTO> findProductById(@PathVariable Long id) throws Exception {
        try {
        	ProductDTO prodDTO = productService.getProductById(id);
            return ResponseEntity.ok(prodDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build(); 
        }
    }
    
    // API para registrar un nuevo producto.
    @PostMapping(value="registration-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> registerProducto(@RequestPart("file") MultipartFile file, @RequestPart ProductRegistrationDTO request) throws Exception
    {	
    	 if (file.isEmpty() || request == null) {
    	        return ResponseEntity.badRequest().body(new ApiResponse("Error: el archivo de imagen o la solicitud están vacíos"));
    	    } else {
    	        try {
    	         
    	            productService.saveProduct(request, file);
    	            historyService.createHistory(TableType.PRODUCTO,ActionType.CREATE);
    	            return ResponseEntity.ok(new ApiResponse("El producto se registró correctamente"));
    	        } catch (IOException ex) {
    	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Error al guardar la imagen: " + ex.getMessage()));
    	        }
    	    }
    }
    
    // API para eleminar un producto.
    @DeleteMapping("/delete-product/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id) throws Exception {
        try {
        	productService.deleteProduct(id);
        	historyService.createHistory(TableType.PRODUCTO,ActionType.DELETE);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build();
        }
    }
    
    // API para actualizar un producto.
    @PutMapping(value="update-product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @RequestPart(value = "file", required = false) MultipartFile file, @RequestPart(value = "product", required = true) ProductDTO product, @RequestParam(value = "keepCurrentImage", required = false) Boolean keepCurrentImage) throws Exception {
    	 if (product == null || (file == null && !keepCurrentImage)) {
    		 return ResponseEntity.badRequest().body(new ApiResponse("Error: el archivo de imagen o la solicitud están vacíos"));
 	    } else {
 	    	try {;
 	    		
 	    	 	// Si file no es nulo, significa que hay una nueva imagen seleccionada, entonces la actualizamos.
	 	       if (file != null) {
	 	           		// Procesar la nueva imagen
	 	    	   	    System.out.print("entre file update");
	 	    	   		product.setId(id);
	 	    	   		productService.updateProduct(product, file);  
	 	       } else if (Boolean.TRUE.equals(keepCurrentImage)) {
	 	           		// Mantener la imagen actual
	 	    	   	  System.out.print("entre data update");
		 	    	  product.setId(id);
		 	    	  productService.updateProductDate(product);
	 	       }
	 	      historyService.createHistory(TableType.PRODUCTO,ActionType.UPDATE);
 	        	 return ResponseEntity.ok(new ApiResponse("Producto actualizado con éxito"));
 	        } catch (RuntimeException e) {
 	            return ResponseEntity.noContent().build(); 
 	        }
 	    }
    }
    
    // API para obtener todos los usuarios.
    @GetMapping("/get-all")
    public ResponseEntity<List<ProductDTO>> findAll() throws Exception {
    	
    	try {
        	List<ProductDTO> categoryAll = productService.getAllProduct();
            return ResponseEntity.ok(categoryAll);
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build(); 
        }
    }
    
    
    // API para obtener todos los productos por busqueda.
    @GetMapping("/search-products")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@RequestParam String name) {
        List<ProductDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }
    // API para obtener el producto mas buscado
    @GetMapping("/most-searched")
    public ResponseEntity<SearchHistory> getMostSearchedProduct() {
    	try {
    		SearchHistory searchHistory = productService.getMostSearchedProduct();
            return ResponseEntity.ok(searchHistory);
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build(); 
        }
    }
    
    // Endpoint para buscar productos por rango de precios
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> searchProductsByPriceRange(@RequestParam Double priceStart, @RequestParam Double priceEnd) {
    	List<ProductDTO> products = productService.searchProductsByPriceRange(priceStart, priceEnd);
    	return ResponseEntity.ok(products);
    }
    
    // API para obtener todos los productos por busqueda.
    @GetMapping("/search-highlight")
    public ResponseEntity<List<ProductDTO>> getAllProductsByHighlights() {
        List<ProductDTO> products = productService.filterProductHighlights();
        return ResponseEntity.ok(products);
    }
    
    // API para obtener todos los productos por categoria.
    @GetMapping("/categoria/{categoryName}")
    public  ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String categoryName) {
        try {
        	 List<ProductDTO> products = productService.findProductsByCategoria(categoryName);
            
            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(Collections.emptyList()); // Devuelve una lista vacía
            }
            
            return ResponseEntity.ok(products);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Collections.emptyList()); // Devuelve una lista vacía
        }
        
    }
    
    // API para contar productos.
    @GetMapping("/count-product")
    public  ResponseEntity<Integer> getCountProducts() {
    	try {
            int total = productService.countAllProducts();
            
            if (total == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(0);
            }
            return ResponseEntity.ok(total);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.noContent().build(); 
        }
        
    }
    
    
 
}	
