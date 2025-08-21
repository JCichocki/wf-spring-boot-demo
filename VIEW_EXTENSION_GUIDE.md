# Spring Boot View Extension Guide

This guide explains how to extend and add new views to the Wakefern Spring Boot Demo application. The application uses **Thymeleaf** as the template engine with **Bootstrap 5** for styling and **HTMX** for enhanced interactions.

## Table of Contents
- [Project Structure](#project-structure)
- [Template System](#template-system)
- [Creating New Views](#creating-new-views)
- [Controller Patterns](#controller-patterns)
- [Static Resources](#static-resources)
- [Internationalization](#internationalization)
- [Form Handling](#form-handling)
- [Best Practices](#best-practices)
- [Complete Example](#complete-example)

## Project Structure

### Template Directory (`src/main/resources/templates/`)
```
templates/
├── layout.html              # Master layout template
├── error.html               # Error page template
├── fragments/
│   └── forms.html           # Reusable form components
├── home/
│   └── index.html           # Home page
├── upload/                  # Upload module templates
│   ├── list.html           # List view
│   ├── add.html            # Add form
│   └── edit.html           # Edit form
└── processes/
    └── list.html           # Process list view
```

### Static Resources (`src/main/resources/static/`)
```
static/
├── css/
│   └── app.css             # Custom application styles
├── js/
│   └── app.js              # Custom JavaScript
├── images/
│   └── logo.png            # Application assets
└── favicon.ico             # Site favicon
```

## Template System

### Layout Template (`layout.html`)
The application uses Thymeleaf Layout Dialect for consistent page structure:

- **Master template**: `layout.html` defines the common structure
- **Bootstrap 5**: Included via WebJars
- **HTMX**: Integrated for enhanced interactions
- **Navigation**: Auto-generated from controller mappings
- **Flash messages**: Success/info/error message display
- **Internationalization**: Messages via `#{key}` syntax

### Page Template Structure
All pages should follow this structure:

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">
    <head>
        <title>[[#{page.title.key}]]</title>
    </head>
    <body>
        <div layout:fragment="content">
            <!-- Your page content here -->
        </div>
    </body>
</html>
```

## Creating New Views

### Step 1: Create Template Directory
For a new module (e.g., `products`):
```bash
mkdir -p src/main/resources/templates/products
```

### Step 2: Create Template Files
Follow the naming convention from existing modules:
- `list.html` - List/index view
- `add.html` - Create form
- `edit.html` - Edit form
- `view.html` - Detail view (if needed)

### Step 3: Add Internationalization Keys
Add messages to `src/main/resources/messages.properties`:
```properties
product.list.headline=Products
product.list.createNew=Create new Product
product.list.empty=No Products could be found.
product.list.edit=Edit
product.list.delete=Delete
product.add.headline=Add Product
product.edit.headline=Edit Product
product.name.label=Name
product.description.label=Description
```

## Controller Patterns

### Basic Controller Structure
```java
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(final ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("products", productService.findAll());
        return "product/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("product") final ProductDTO productDTO) {
        return "product/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("product") @Valid final ProductDTO productDTO,
            final BindingResult bindingResult, 
            final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "product/add";
        }
        productService.create(productDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, 
            WebUtils.getMessage("product.create.success"));
        return "redirect:/products";
    }
}
```

### Navigation Integration
Add new menu items to `layout.html`:
```html
<ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarEntitiesLink">
    <li><a th:href="@{/uploads}" class="dropdown-item">[[#{upload.list.headline}]]</a></li>
    <li><a th:href="@{/products}" class="dropdown-item">[[#{product.list.headline}]]</a></li>
</ul>
```

## Static Resources

### CSS Organization
Add custom styles to `src/main/resources/static/css/app.css`:
```css
/* Custom component styles */
.product-card {
    border: 1px solid #dee2e6;
    border-radius: 0.375rem;
    padding: 1rem;
    margin-bottom: 1rem;
}

/* Utility classes */
.text-muted-light {
    color: #8e9194 !important;
}
```

### JavaScript Integration
Add custom JavaScript to `src/main/resources/static/js/app.js`:
```javascript
// Custom application JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize components
    initializeProductFilters();
});

function initializeProductFilters() {
    // Custom functionality
}
```

### Images and Assets
Place static assets in appropriate directories:
- `static/images/` - Images and graphics
- `static/fonts/` - Custom fonts (if needed)
- `static/icons/` - Icon files

## Internationalization

### Message Keys Convention
Follow the pattern: `[module].[view].[element]`
```properties
# Module headlines
product.list.headline=Products
product.add.headline=Add Product
product.edit.headline=Edit Product

# Field labels
product.name.label=Product Name
product.description.label=Description
product.price.label=Price

# Actions
product.list.createNew=Create new Product
product.list.edit=Edit
product.list.delete=Delete

# Messages
product.create.success=Product was created successfully.
product.update.success=Product was updated successfully.
product.delete.success=Product was removed successfully.
```

### Template Usage
```html
<!-- Headlines -->
<h1>[[#{product.list.headline}]]</h1>

<!-- Labels -->
<label>[[#{product.name.label}]]</label>

<!-- Links -->
<a th:href="@{/products/add}">[[#{product.list.createNew}]]</a>
```

## Form Handling

### Using Form Fragments
The application provides reusable form components in `fragments/forms.html`:

```html
<!-- Text input -->
<div th:replace="~{fragments/forms :: inputRow('product', 'name', type='text', required=true)}" />

<!-- Textarea -->
<div th:replace="~{fragments/forms :: inputRow('product', 'description', type='textarea')}" />

<!-- Select dropdown -->
<div th:replace="~{fragments/forms :: inputRow('product', 'category', type='select')}" />

<!-- Checkbox -->
<div th:replace="~{fragments/forms :: inputRow('product', 'active', type='checkbox')}" />
```

### Form Template Structure
```html
<form th:action="@{/products/add}" th:object="${product}" method="post" class="needs-validation" novalidate>
    <!-- Form fields using fragments -->
    <div th:replace="~{fragments/forms :: inputRow('product', 'name', type='text', required=true)}" />
    <div th:replace="~{fragments/forms :: inputRow('product', 'description', type='textarea')}" />
    
    <!-- Global errors -->
    <div th:replace="~{fragments/forms :: globalErrors('product')}" />
    
    <!-- Actions -->
    <div class="row mb-3">
        <div class="col-md-10 offset-md-2">
            <button type="submit" class="btn btn-primary">[[#{product.add.submit}]]</button>
            <a th:href="@{/products}" class="btn btn-secondary ms-2">[[#{product.add.back}]]</a>
        </div>
    </div>
</form>
```

### Validation Integration
The form fragments automatically handle validation errors:
- Field-level errors are displayed below each input
- Invalid fields get the `is-invalid` CSS class
- Bootstrap styling provides visual feedback

## Best Practices

### 1. Naming Conventions
- **Templates**: Use module/action pattern (`product/list.html`)
- **Controllers**: End with `Controller` (`ProductController`)
- **URLs**: Use RESTful patterns (`/products`, `/products/add`)
- **CSS classes**: Use BEM methodology or utility classes

### 2. Security Considerations
- Always validate form inputs with `@Valid`
- Use CSRF protection (enabled by default)
- Sanitize user input in templates with `[[]]` syntax
- Use proper HTTP methods (GET for display, POST for modifications)

### 3. Performance Optimization
- Use fragment caching for expensive operations
- Optimize static resource loading
- Minimize JavaScript and CSS
- Use appropriate HTTP caching headers

### 4. Responsive Design
- Use Bootstrap responsive utilities
- Test on multiple screen sizes
- Consider mobile-first design
- Use appropriate breakpoints

### 5. Accessibility
- Use semantic HTML elements
- Provide proper ARIA labels
- Ensure keyboard navigation works
- Maintain proper color contrast

## Complete Example

Here's a complete example of adding a "Products" module:

### 1. Controller (`ProductController.java`)
```java
package com.wakefern.sbdemo.product;

import com.wakefern.sbdemo.util.WebUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(final ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String list(final Model model) {
        model.addAttribute("products", productService.findAll());
        return "product/list";
    }

    @GetMapping("/add")
    public String add(@ModelAttribute("product") final ProductDTO productDTO) {
        return "product/add";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("product") @Valid final ProductDTO productDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "product/add";
        }
        productService.create(productDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("product.create.success"));
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id, final Model model) {
        model.addAttribute("product", productService.get(id));
        return "product/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") final Long id,
            @ModelAttribute("product") @Valid final ProductDTO productDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "product/edit";
        }
        productService.update(id, productDTO);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS, WebUtils.getMessage("product.update.success"));
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable(name = "id") final Long id,
            final RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO, WebUtils.getMessage("product.delete.success"));
        return "redirect:/products";
    }
}
```

### 2. List Template (`templates/product/list.html`)
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
        layout:decorate="~{layout}">
    <head>
        <title>[[#{product.list.headline}]]</title>
    </head>
    <body>
        <div layout:fragment="content">
            <div class="d-flex flex-wrap mb-4">
                <h1 class="flex-grow-1">[[#{product.list.headline}]]</h1>
                <div>
                    <a th:href="@{/products/add}" class="btn btn-primary ms-2">[[#{product.list.createNew}]]</a>
                </div>
            </div>
            <div th:if="${products.empty}">[[#{product.list.empty}]]</div>
            <div th:if="${!products.empty}" class="table-responsive">
                <table class="table table-striped table-hover align-middle">
                    <thead>
                        <tr>
                            <th scope="col">[[#{product.id.label}]]</th>
                            <th scope="col">[[#{product.name.label}]]</th>
                            <th scope="col">[[#{product.price.label}]]</th>
                            <th><!-- --></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="product : ${products}">
                            <td>[[${product.id}]]</td>
                            <td>[[${product.name}]]</td>
                            <td>[[${product.price}]]</td>
                            <td>
                                <div class="float-end text-nowrap">
                                    <a th:href="@{/products/edit/{id}(id=${product.id})}" class="btn btn-sm btn-secondary">[[#{product.list.edit}]]</a>
                                    <form th:action="@{/products/delete/{id}(id=${product.id})}"
                                            th:hx-confirm="#{delete.confirm}" method="post" class="d-inline">
                                        <button type="submit" class="btn btn-sm btn-secondary">[[#{product.list.delete}]]</button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
```

### 3. Add Form Template (`templates/product/add.html`)
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org" xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
        layout:decorate="~{layout}">
    <head>
        <title>[[#{product.add.headline}]]</title>
    </head>
    <body>
        <div layout:fragment="content">
            <div class="d-flex flex-wrap mb-4">
                <h1 class="flex-grow-1">[[#{product.add.headline}]]</h1>
                <div>
                    <a th:href="@{/products}" class="btn btn-secondary ms-2">[[#{product.add.back}]]</a>
                </div>
            </div>
            <form th:action="@{/products/add}" th:object="${product}" method="post" class="needs-validation" novalidate>
                <div th:replace="~{fragments/forms :: inputRow('product', 'name', type='text', required=true)}" />
                <div th:replace="~{fragments/forms :: inputRow('product', 'description', type='textarea')}" />
                <div th:replace="~{fragments/forms :: inputRow('product', 'price', type='number', required=true)}" />
                <div th:replace="~{fragments/forms :: globalErrors('product')}" />
                <div class="row mb-3">
                    <div class="col-md-10 offset-md-2">
                        <button type="submit" class="btn btn-primary">[[#{product.add.submit}]]</button>
                        <a th:href="@{/products}" class="btn btn-secondary ms-2">[[#{product.add.back}]]</a>
                    </div>
                </div>
            </form>
        </div>
    </body>
</html>
```

### 4. Message Properties (additions to `messages.properties`)
```properties
# Product module
product.list.headline=Products
product.list.createNew=Create new Product
product.list.empty=No Products could be found.
product.list.edit=Edit
product.list.delete=Delete
product.add.headline=Add Product
product.add.back=Back to list
product.add.submit=Create Product
product.edit.headline=Edit Product
product.edit.back=Back to list
product.edit.submit=Update Product
product.id.label=ID
product.name.label=Product Name
product.description.label=Description
product.price.label=Price
product.create.success=Product was created successfully.
product.update.success=Product was updated successfully.
product.delete.success=Product was removed successfully.
```

### 5. Navigation Update (`layout.html` - add to dropdown)
```html
<li><a th:href="@{/products}" class="dropdown-item">[[#{product.list.headline}]]</a></li>
```

---

This guide provides a comprehensive foundation for extending your Spring Boot application with new views while maintaining consistency with existing patterns and best practices.