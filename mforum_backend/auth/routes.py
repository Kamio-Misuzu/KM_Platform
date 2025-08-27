from datetime import datetime

from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from werkzeug.security import generate_password_hash, check_password_hash
from models import User
from database import db
from .utils import validate_email, validate_password

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/register', methods=['POST'])
def register():
    try:
        data = request.get_json()

        # 验证必要字段
        if not data or not data.get('username') or not data.get('email') or not data.get('password'):
            return jsonify({'error': 'Missing required fields'}), 400

        username = data['username']
        email = data['email']
        password = data['password']

        # 验证用户名长度
        if len(username) < 3:
            return jsonify({'error': 'Username too short'}), 400

        # 验证密码强度
        if not validate_password(password):
            return jsonify({'error': 'Password must be at least 6 characters'}), 400

        # 验证邮箱格式
        if not validate_email(email):
            return jsonify({'error': 'Invalid email format'}), 400

        # 检查用户名和邮箱是否已存在
        if User.query.filter_by(username=username).first():
            return jsonify({'error': 'Username already exists'}), 400

        if User.query.filter_by(email=email).first():
            return jsonify({'error': 'Email already exists'}), 400

        # 创建新用户
        hashed_password = generate_password_hash(password)
        new_user = User(
            username=username,
            email=email,
            password=hashed_password
        )

        db.session.add(new_user)
        db.session.commit()

        # 生成访问令牌
        access_token = create_access_token(identity=new_user.id)

        return jsonify({
            'message': 'User created successfully',
            'user': new_user.to_dict(),
            'access_token': access_token
        }), 201

    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@auth_bp.route('/login', methods=['POST'])
def login():
    try:
        data = request.get_json()

        if not data or not data.get('username') or not data.get('password'):
            return jsonify({'error': 'Missing username or password'}), 400

        username = data['username']
        password = data['password']

        # 查找用户
        user = User.query.filter_by(username=username).first()

        if not user or not check_password_hash(user.password, password):
            return jsonify({'error': 'Invalid username or password'}), 401

        # 更新最后登录时间
        user.last_login = int(datetime.now().timestamp() * 1000)
        db.session.commit()

        # 生成访问令牌
        access_token = create_access_token(identity=user.id)

        return jsonify({
            'message': 'Login successful',
            'user': user.to_dict(),
            'access_token': access_token
        }), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500