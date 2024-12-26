<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>HAR Comparison Report</title>
    <style>
        .table-container {
            display: flex;
            justify-content: space-evenly; /* Размещаем таблицы с равными промежутками */
            gap: 20px; /* Промежуток между таблицами */
        }
        table {
            width: 40%; /* Ширина таблицы, можно настроить по необходимости */
            border-collapse: collapse;
        }
        table, th, td {
            border: 1px solid black;
        }
        th, td {
            padding: 10px;
            text-align: left;
        }
        .truncate {
            max-width: 400px; /* Максимальная ширина ячейки */
            white-space: nowrap; /* Запрещаем перенос текста на новую строку */
            overflow: hidden; /* Скрываем переполнение */
            text-overflow: ellipsis; /* Добавляем многоточие */
        }
    </style>
</head>
<body>
<h1>HAR Comparison Report</h1>

<h2>Blocked URLs not in New HAR 1 (Total: ${blockedUrlsNotInNewHar1?size})</h2>
<table>
    <tr>
        <th>Blocked URLs</th>
    </tr>
    <#list blockedUrlsNotInNewHar1 as url>
        <tr>
            <td class="truncate">${url}</td>
        </tr>
    </#list>
</table>

<div class="table-container">
    <div>
        <h2>Blocked URLs in MITM (Total: ${blockedUrlsCountNewHar1})</h2>
        <table>
            <tr>
                <th>Blocked URLs</th>
            </tr>
            <#list blockedUrlsNewHar1 as url>
                <tr>
                    <td class="truncate">${url}</td>
                </tr>
            </#list>
        </table>
    </div>
    <div>
        <h2>Blocked URLs in ADblock (Total: ${blockedUrlsCountNewHar2})</h2>
        <table>
            <tr>
                <th>Blocked URLs</th>
            </tr>
            <#list blockedUrlsNewHar2 as url>
                <tr>
                    <td class="truncate">${url}</td>
                </tr>
            </#list>
        </table>
    </div>
    <div>
        <h2>Blocked URLs in ADguard (Total: ${blockedUrlsCountNewHar3})</h2>
        <table>
            <tr>
                <th>Blocked URLs</th>
            </tr>
            <#list blockedUrlsNewHar3 as url>
                <tr>
                    <td class="truncate">${url}</td>
                </tr>
            </#list>
        </table>
    </div>
</div>
</body>
</html>
