jQuery(document).ready(() => {
    jQuery("td.timestamp").each(function() {
        const dt = new Date($(this).text());
        $(this).text(dt.toLocaleString("en-GB"));
    });
});
