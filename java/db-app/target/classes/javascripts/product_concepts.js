// NOTE: The api_host needs to be replaced with the url of the web service
// being used to access the DrugBank API, and the urls and parameters
// in the AJAX calls below will need to be updated accordingly.
// This example assumes the request urls and response json is in the same
// format as if the DrugBank API was being accessed directly.
var localhost = "/api/";
var api_host = "https://api.drugbankplus.com/v1/";

highlight_name = function(concept) {
    var name = concept.name;
    concept.hits.forEach(function(h) {
        name = name.replace(strip_em_tags(h.value), h.value);
    });
    return name;
};

strip_em_tags = function(text) {
    return text.replace(/<\/?em>/g, "");
};

em_to_u_tags = function(text) {
    text = text.replace("<em>", "<u>").replace("</em>", "</u>");
    if (text.includes("<em>")) {
        return em_to_u_tags(text);
    } else {
        return text;
    }
};

// Display the API request and response on the page
displayRequest = function(url, data) {
    $(".http-request").html("GET " + url);
    $(".shell-command").html("curl -L '" + url + "' -H 'Authorization: mytoken'");
    $(".api-response").html(Prism.highlight(JSON.stringify(JSON.parse(data), null, 2), Prism.languages.json));
};

// Load the results into the table
loadTableResults = function(products_table, data) {
    if ($(".drug_autocomplete").val() && $(".route_autocomplete").val() && $(".strength_autocomplete").val()) {
        JSON.parse(data).forEach(function(d) {
            products_table.row.add([d.name, d.dosage_form, getStrengths(d), d.route, d.labeller.name]);
        });
        products_table.draw();
    }
};

getStrengths = function(d) {
    return d.ingredients.map(function(i) {
        return i.name + " " + i.strength.number + " " + i.strength.unit;
    }).join("<br>");
};

handleError = function(jqXHR, element) {
    var message;
    if ((jqXHR.status && jqXHR.status === 0) || (jqXHR.statusText && jqXHR.statusText === 'abort')) {
        return;
    } else {
        $(element).val(null).trigger('change');
        try {
            if ($.parseJSON(jqXHR.responseText).errors) {
                message = $.parseJSON(jqXHR.responseText).errors.join();
            } else {
                message = jqXHR.responseText;
            }
        } catch (_error) {
            message = jqXHR.responseText || "Error accessing " + localhost;
        }
        return alert(message + ". Please wait and try again or contact the DrugBank Team.");
    }
};

clearTableResults = function(table) {
    table.clear().draw();
};

$(document).ready(function() {
    var products_table = $('.products-table').DataTable({
        order: [
            [0, "desc"]
        ]
    });

    // Activate the drug search input
    $(".drug_autocomplete").select2({
        theme: "bootstrap",
        placeholder: "Start typing a drug name",
        minimumInputLength: 3,
        templateResult: function(d) {
            return $('<span>' + em_to_u_tags(d.text) + '</span>');
        },
        templateSelection: function(d) {
            return $('<span>' + strip_em_tags(d.text) + '</span>');
        },
        ajax: {
            url: localhost + encodeURI("product_concepts"),
            delay: 100,
            data: function(params) {
                return {
                    q: encodeURI(params.term)
                };
            },
            processResults: function(data) {
                return {
                    results: $.map(JSON.parse(data), function(i) {
                        return {
                            id: i.drugbank_pcid,
                            text: highlight_name(i)
                        };
                    })
                };
            },
            success: function(data) {
                var url = encodeURI(api_host + "product_concepts?q=" + $(".select2-search__field").val());
                displayRequest(url, data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                handleError(jqXHR, ".drug_autocomplete");
            }
        }
    });

    // Load the product concept routes when a drug is selected
    $("#product-concepts-tutorial .drug_autocomplete").on("change", function(e) {
        $(".route_autocomplete").empty();
        $(".strength_autocomplete").empty();
        clearTableResults(products_table);
        if ($(this).val()) {
            var path = encodeURI("product_concepts/" + $(this).val() + "/routes")
            $.ajax({
                url: localhost + path,
                delay: 100,
                success: function(data) {
                    displayRequest(api_host + path, data);
                    JSON.parse(data).map(function(d) {
                        return {
                            route: d.route,
                            id: d.drugbank_pcid
                        };
                    }).sort(function(a, b) {
                        return a.route.localeCompare(b.route);
                    }).forEach(function(r) {
                        $(".route_autocomplete").append(new Option(r.route, r.id, false, false));
                    });
                    $(".route_autocomplete").val(null).trigger('change');
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    handleError(jqXHR, ".drug_autocomplete");
                }
            });
        }
    });

    // Activate the route input
    $(".route_autocomplete").select2({
        theme: "bootstrap",
        placeholder: "Select a route"
    });

    // Load the product concept strengths when a route is selected
    $("#product-concepts-tutorial .route_autocomplete").on("change", function(e) {
        $(".strength_autocomplete").empty();
        clearTableResults(products_table);
        if ($(this).val()) {
            var path = encodeURI("product_concepts/" + $(this).val() + "/strengths")
            $.ajax({
                url: localhost + path,
                delay: 100,
                success: function(data) {
                    displayRequest(api_host + path, data);
                    JSON.parse(data).map(function(d) {
                        return {
                            strength: d.name,
                            id: d.drugbank_pcid
                        };
                    }).sort().forEach(function(r) {
                        $(".strength_autocomplete").append(new Option(r.strength, r.id, false, false));
                    });
                    $(".strength_autocomplete").val(null).trigger('change');
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    handleError(jqXHR, ".route_autocomplete");
                }
            });
        }
    });

    // Activate the strength input
    $(".strength_autocomplete").select2({
        theme: "bootstrap",
        placeholder: "Select a strength"
    });

    // Load the product concepts when a strength is selected
    $("#product-concepts-tutorial .strength_autocomplete").on("change", function(e) {
        clearTableResults(products_table);
        if ($(this).val()) {
            $("#loader").show();
            var path = encodeURI("product_concepts/" + $(this).val() + "/products")
            $.ajax({
                url: localhost + path,
                delay: 100,
                success: function(data) {
                    displayRequest(api_host + path, data);
                    loadTableResults(products_table, data);
                    $("#loader").hide();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    handleError(jqXHR, ".strength_autocomplete");
                    $("#loader").hide();
                }
            });
        }
    });
});