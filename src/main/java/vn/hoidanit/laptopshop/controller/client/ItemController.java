package vn.hoidanit.laptopshop.controller.client;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ItemController {

    private final ProductService productService;
    private final UserService userService;

    public ItemController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/product/{id}")
    public String getMethodName(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        Product product = this.productService.fetchProductById(id).get();
        model.addAttribute("product", product);

        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String postMethodName(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long productId = id;
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, productId, session, 1);
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        User currentUser = new User();
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            totalPrice += cartDetail.getPrice() * cartDetail.getQuantity();
        }
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("cart", cart);

        model.addAttribute("totalPrice", totalPrice);
        return "client/cart/show";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckOutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailId = id;
        this.productService.handleRemoveCartDetail(cartDetailId, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        User currentUser = new User();
        currentUser.setId(id);

        Cart cart = this.productService.fetchByUser(currentUser);
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();
        model.addAttribute("cartDetails", cartDetails);

        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            totalPrice += cartDetail.getPrice() * cartDetail.getQuantity();
        }

        model.addAttribute("totalPrice", totalPrice);
        return "client/cart/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {
        HttpSession session = request.getSession(false);

        User currentUser = new User();
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone);

        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String getThankYouPage(Model model) {
        return "client/cart/thanks";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(@RequestParam("id") long id,
            @RequestParam("quantity") long quantity,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, id, session, quantity);
        return "redirect:/product/" + id;
    }

    @GetMapping("/products")
    public String getProductPage(Model model,
            @RequestParam("page") Optional<String> pageOptional,
            @RequestParam("name") Optional<String> nameOptional,
            @RequestParam("factory") Optional<String> factoryOptional,
            @RequestParam("target") Optional<String> targetOptional,
            @RequestParam("price") Optional<String> priceOptional,
            @RequestParam("sort") Optional<String> sortOptional

    ) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                // convert from String to int
                page = Integer.parseInt(pageOptional.get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        Pageable pageable = PageRequest.of(page - 1, 10);

        String name = nameOptional.isPresent() ? nameOptional.get() : "";
        Page<Product> prs = this.productService.fetchProducts(pageable,
                name);

        // case 1
        // double min = minOptional.isPresent() ? Double.parseDouble(minOptional.get())
        // : 0;
        // Page<Product> prs = this.productService.fetchProducts(pageable, min);

        // case 2
        // double max = maxOptional.isPresent() ? Double.parseDouble(maxOptional.get())
        // : 0;
        // Page<Product> prs = this.productService.fetchProducts(pageable, max);

        // case 3
        // String factory = factoryOptional.isPresent() ? factoryOptional.get() : "";
        // Page<Product> prs = this.productService.fetchProducts(pageable,
        // factory);

        // case 4
        // List<String> factory = Arrays.asList(factoryOptional.get().split(","));
        // Page<Product> prs = this.productService.fetchProducts(pageable,
        // factory);

        // case 5
        // String price = priceOptional.isPresent() ? priceOptional.get() : "";
        // Page<Product> prs = this.productService.fetchProducts(pageable,
        // price);

        // case 6
        // List<String> price = Arrays.asList(priceOptional.get().split(","));
        // Page<Product> prs = this.productService.fetchProducts(pageable,
        // price);

        List<Product> products = prs.getContent();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", prs.getTotalPages());
        return "client/product/show";
    }

}
