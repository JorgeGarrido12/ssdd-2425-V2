import time
from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
from models import User, users

#Importos para probar lo de usar sesion en vez de User.get_user(email) con REST.
from functools import wraps
from flask import redirect, url_for, session
from datetime import datetime

import requests
import os

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

#@app.route('/static/<path:path>')
#def serve_static(path):
#    return send_from_directory('static', path)

def session_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if "user_id" not in session or "token_privado" not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))

    error = None
    form = LoginForm(None if request.method != 'POST' else request.form)

    if request.method == 'POST' and form.validate():
        email = form.email.data
        password = form.password.data

        # Aquí llamamos al backend REST
        try:
            url_backend = "http://backend-rest:8080/Service/checkLogin"

            payload = {
                "email": email,
                "password": password
            }
            response = requests.post(url_backend, json=payload)

            if response.status_code == 200:
                data = response.json()
                # Ejemplo de respuesta esperada:
                # {
                #   "userId": "2",
                #   "token": "TOKENPRIVADO...."
                # }
                session["user_id"] = data["id"]
                session["token_privado"] = data["token"]

                return redirect(url_for('profile'))

            else:
                error = f"Login incorrecto: {response.status_code} {response.text}"

        except Exception as e:
            error = f"Error de conexión con el backend: {str(e)}"

    return render_template('login.html', form=form, error=error)



@app.route('/signup', methods=['GET', 'POST'])
def signup():
    form = RegisterForm()

    if request.method == 'POST' and form.validate_on_submit():
        email = form.email.data
        name = form.username.data
        password = form.password.data
        password2 = form.confirm.data

        if password != password2:
            error = "Las contraseñas no coinciden"
            return render_template('signup.html', form=form, error=error)

        # Petición al backend REST
        try:
            url_backend =  "http://backend-rest:8080/Service/register"

            payload = {
                "id": name,
                "email": email,
                "name": name,
                "password": password
            }

            response = requests.post(url_backend, json=payload)

            if response.status_code == 201:
                user_data = response.json()
                session["user_id"] = user_data["id"]
                session["token_privado"] = user_data["token"]

                return redirect(url_for('profile'))

            else:
                if response.status_code == 409:
                    error_msg = response.json().get("error", "El email ya está registrado.")
                    error = error_msg
                else:
                    error = f"Error al registrar usuario: {response.status_code}"
                return render_template('signup.html', form=form, error=error)


        except Exception as e:
            error = f"Error al conectar con el backend: {str(e)}"
            return render_template('signup.html', form=form, error=error)

    return render_template('signup.html', form=form)


@app.route('/profile')
#@login_required
@session_required
def profile():
    try:
        endpoint_url = f"/Service/u/{session['user_id']}"   # para Auth-Token
        full_url = f"http://backend-rest:8080{endpoint_url}"           # para requests.get()

        headers = build_auth_headers(endpoint_url)                  # para cabeceras

        response = requests.get(full_url, headers=headers)          # la llamada HTTP

        if response.status_code == 200:
            user_data = response.json()
            # Puedes pasar los datos al template:
            return render_template('profile.html', user_data=user_data)
        else:
            error = f"Error al obtener perfil: {response.status_code} {response.text}"
            return render_template('profile.html', error=error)

    except Exception as e:
        error = f"Error al conectar con el backend: {str(e)}"
        return render_template('profile.html', error=error)


@app.route('/logout')
@session_required
def logout():
    logout_user()
    session.clear()  # ← esto borra todo el contenido de la sesión
    return redirect(url_for('index'))


@app.route('/new-conversation', methods=['GET', 'POST'])
#@login_required
@session_required
def new_conversation():
    if request.method == 'POST':
        title = request.form.get('title')
        first_message = request.form.get('first_message')

        endpoint_url = f"/Service/u/{session['user_id']}/dialogue"
        full_url = f"http://backend-rest:8080{endpoint_url}"
        headers = build_auth_headers(endpoint_url)

        payload = {
            "dialogueId": title
            # puedes meter otros campos si tu endpoint REST los acepta
        }

        # Hacemos POST al backend REST
        response = requests.post(full_url, json=payload, headers=headers)

        if response.status_code == 201:
            # conversación creada → redirect a /conversations
            return redirect(url_for('conversations'))
        else:
            error = f"Error al crear conversación: {response.status_code} {response.text}"
            return render_template('new_conversation.html', error=error)

    return render_template('new_conversation.html')


@app.route('/conversations')
@session_required
def conversations():
    endpoint_url = f"/Service/u/{session['user_id']}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    try:
        response = requests.get(full_url, headers=headers)

        if response.status_code == 200:
            user_data = response.json()
            convs = user_data.get("dialogues", [])
            return render_template('conversations.html', convs=convs)
        else:
            error = f"Error al obtener conversaciones: {response.status_code} {response.text}"
            return render_template('conversations.html', error=error)

    except Exception as e:
        error = f"Error de conexión: {str(e)}"
        return render_template('conversations.html', error=error)




@app.route('/chat/<dialogue_id>', methods=['GET', 'POST'])
@session_required
def chat_detail(dialogue_id):
    # Paso 1: obtener la conversación del backend
    endpoint_url = f"/Service/u/{session['user_id']}/dialogue/{dialogue_id}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    response = requests.get(full_url, headers=headers)

    if response.status_code != 200:
        error = f"Error al obtener conversación: {response.status_code} {response.text}"
        return render_template('chat_detail.html', error=error)

    conv = response.json()

    # Paso 2: si es POST, enviar nuevo prompt
    if request.method == 'POST':
        prompt_text = request.form.get('prompt')

        if prompt_text and conv.get("status") == "READY":
            prompt_endpoint = f"/Service/u/{session['user_id']}/dialogue/{dialogue_id}/next"
            full_next_url = f"http://backend-rest:8080{prompt_endpoint}"
            headers_next = build_auth_headers(prompt_endpoint)

            payload = {
                "prompt": prompt_text
            }

            print("DEBUG FULL_NEXT_URL:", full_next_url)
            post_resp = requests.post(full_next_url, json=payload, headers=headers_next)

            if post_resp.status_code not in (200, 201, 202):
                error = f"Error al enviar prompt: {post_resp.status_code} {post_resp.text}"
                return render_template('chat_detail.html', conv=conv, error=error)

            return redirect(url_for('chat_detail', dialogue_id=dialogue_id))

    return render_template('chat_detail.html', conv=conv)




@app.route('/end_conversation/<dialogue_id>', methods=['POST'])
@session_required
def end_conversation(dialogue_id):
    # Construir la URL exacta y segura al endpoint REST
    endpoint_url = f"/Service/u/{session['user_id']}/dialogue/{dialogue_id}/end"
    full_end_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    # Hacemos el POST al backend para finalizar la conversación
    try:
        post_resp = requests.post(full_end_url, json={}, headers=headers)

        if post_resp.status_code in (200, 201, 202):
            # Volvemos a consultar la conversación actualizada tras finalizarla
            get_url = f"http://backend-rest:8080/Service/u/{session['user_id']}/dialogue/{dialogue_id}"
            headers_get = build_auth_headers(f"/Service/u/{session['user_id']}/dialogue/{dialogue_id}")

            refreshed = requests.get(get_url, headers=headers_get)

            if refreshed.status_code == 200:
                conv = refreshed.json()
                return render_template('chat_detail.html', conv=conv)
            else:
                return redirect(url_for('chat_detail', dialogue_id=dialogue_id))
        else:
            error = f"Error al terminar conversación: {post_resp.status_code} {post_resp.text}"
            return redirect(url_for('chat_detail', dialogue_id=dialogue_id))

    except Exception as e:
        error = f"Excepción al terminar conversación: {str(e)}"
        return redirect(url_for('chat_detail', dialogue_id=dialogue_id))



@app.route('/delete_conversation/<dialogue_id>')
#@login_required
@session_required
def delete_conversation(dialogue_id):
    endpoint_url = f"/Service/u/{session['user_id']}/dialogue/{dialogue_id}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    response = requests.delete(full_url, headers=headers)

    if response.status_code == 200:
        # Ok borrada
        return redirect(url_for('conversations'))
    else:
        error = f"Error al borrar conversación: {response.status_code} {response.text}"
        # Si quieres, podrías volver a conversations igual
        return redirect(url_for('conversations'))

@app.route('/logs')
@session_required
def logs():
    user_id = session["user_id"]
    endpoint_url = f"/Service/u/{user_id}/logs"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    try:
        response = requests.get(full_url, headers=headers)

        if response.status_code == 200:
            logs_data = response.json()
            return render_template("logs.html", logs=logs_data)
        else:
            error = f"Error al obtener logs: {response.status_code} {response.text}"
            return render_template("logs.html", error=error)

    except Exception as e:
        error = f"Excepción al obtener logs: {str(e)}"
        return render_template("logs.html", error=error)

@app.route('/logs/delete/<dialogue_id>', methods=["POST"])
@session_required
def delete_log(dialogue_id):
    user_id = session["user_id"]
    endpoint_url = f"/Service/u/{user_id}/logs/{dialogue_id}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    try:
        response = requests.delete(full_url, headers=headers)
        if response.status_code == 200:
            return redirect(url_for('logs'))
        else:
            error = f"No se pudo eliminar el log: {response.status_code}"
            return render_template("logs.html", error=error)
    except Exception as e:
        error = f"Excepción al eliminar log: {str(e)}"
        return render_template("logs.html", error=error)


@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == int(user_id):
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))



def build_auth_headers(endpoint_url):
    from datetime import datetime
    import hashlib

    date_now = datetime.utcnow().isoformat() + "Z"
    data_to_hash = endpoint_url + date_now + session["token_privado"]
    auth_token = hashlib.md5(data_to_hash.encode("utf-8")).hexdigest()

    return {
        "User": session["user_id"],
        "Date": date_now,
        "Auth-Token": auth_token
    }



@app.template_filter('datetimeformat')
def datetimeformat(value):
    return datetime.utcfromtimestamp(value / 1000).strftime('%Y-%m-%d %H:%M:%S')
